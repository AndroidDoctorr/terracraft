package com.torr.terracraft.geo;

import com.torr.terracraft.config.TerracraftConfig;
import com.torr.terracraft.terracraft;
import net.minecraft.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DemTileCache
{
    private record TileKey(int zoom, int tileX, int tileY)
    {
    }

    private final Path cacheRoot;
    private final String urlTemplate;
    private final ExecutorService downloadExecutor;
    private final HttpClient httpClient;
    private final Map<TileKey, double[][]> memoryCache = new ConcurrentHashMap<>();
    private final Map<TileKey, CompletableFuture<double[][]>> inFlight = new ConcurrentHashMap<>();

    public DemTileCache(Path cacheRoot, String urlTemplate, int downloadThreads)
    {
        this.cacheRoot = cacheRoot;
        this.urlTemplate = urlTemplate;
        this.downloadExecutor = Executors.newFixedThreadPool(downloadThreads, runnable ->
        {
            Thread thread = new Thread(runnable, "Terracraft-DEM");
            thread.setDaemon(true);
            return thread;
        });
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public double sample(int zoom, double latitude, double longitude)
    {
        if (TerracraftConfig.demBilinearSampling.get())
        {
            return sampleBilinear(zoom, latitude, longitude);
        }

        TerrariumTileMath.TileSample sample = TerrariumTileMath.locate(zoom, latitude, longitude);
        double[][] heights = getTileHeights(zoom, sample.tileX(), sample.tileY());
        return heights[sample.pixelX()][sample.pixelY()];
    }

    private double sampleBilinear(int zoom, double latitude, double longitude)
    {
        double worldX = TerrariumTileMath.worldPixelX(longitude, zoom);
        double worldY = TerrariumTileMath.worldPixelY(latitude, zoom);
        int x0 = (int) Math.floor(worldX);
        int y0 = (int) Math.floor(worldY);
        double tx = worldX - x0;
        double ty = worldY - y0;

        double h00 = sampleWorldPixel(zoom, x0, y0);
        double h10 = sampleWorldPixel(zoom, x0 + 1, y0);
        double h01 = sampleWorldPixel(zoom, x0, y0 + 1);
        double h11 = sampleWorldPixel(zoom, x0 + 1, y0 + 1);
        double top = h00 + tx * (h10 - h00);
        double bottom = h01 + tx * (h11 - h01);
        return top + ty * (bottom - top);
    }

    private double sampleWorldPixel(int zoom, int worldPixelX, int worldPixelY)
    {
        int tileX = Math.floorDiv(worldPixelX, 256);
        int tileY = Math.floorDiv(worldPixelY, 256);
        int pixelX = Math.floorMod(worldPixelX, 256);
        int pixelY = Math.floorMod(worldPixelY, 256);
        double[][] heights = getTileHeights(zoom, tileX, tileY);
        return heights[pixelX][pixelY];
    }

    public Path cacheRoot()
    {
        return cacheRoot;
    }

    public void prefetchArea(int zoom, double minLat, double maxLat, double minLon, double maxLon)
    {
        TerrariumTileMath.TileRange range = TerrariumTileMath.tilesCovering(zoom, minLat, maxLat, minLon, maxLon);
        List<CompletableFuture<double[][]>> pending = new ArrayList<>();

        for (int tileX = range.minTileX(); tileX <= range.maxTileX(); tileX++)
        {
            for (int tileY = range.minTileY(); tileY <= range.maxTileY(); tileY++)
            {
                TileKey key = new TileKey(zoom, tileX, tileY);
                if (memoryCache.containsKey(key))
                {
                    continue;
                }
                pending.add(startTileLoad(key));
            }
        }

        if (!pending.isEmpty())
        {
            CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).join();
        }
    }

    private double[][] getTileHeights(int zoom, int tileX, int tileY)
    {
        TileKey key = new TileKey(zoom, tileX, tileY);
        double[][] cached = memoryCache.get(key);
        if (cached != null)
        {
            return cached;
        }

        double[][] loaded = startTileLoad(key).join();
        return loaded != null ? loaded : flatTile();
    }

    private CompletableFuture<double[][]> startTileLoad(TileKey key)
    {
        return inFlight.computeIfAbsent(key, ignored ->
        {
            CompletableFuture<double[][]> pending = CompletableFuture.supplyAsync(
                    () -> loadTileHeights(key),
                    downloadExecutor
            );
            pending.whenComplete((loaded, error) ->
            {
                if (loaded != null)
                {
                    memoryCache.putIfAbsent(key, loaded);
                }
                inFlight.remove(key);
            });
            return pending;
        });
    }

    private double[][] loadTileHeights(TileKey key)
    {
        Path tilePath = cacheRoot.resolve(Integer.toString(key.zoom()))
                .resolve(key.tileX() + "_" + key.tileY() + ".png");

        try
        {
            if (Files.exists(tilePath))
            {
                return decodeTile(ImageIO.read(tilePath.toFile()));
            }

            Files.createDirectories(tilePath.getParent());
            BufferedImage downloaded = downloadTile(key);
            ImageIO.write(downloaded, "png", tilePath.toFile());
            terracraft.LOGGER.info("Cached DEM tile z/x/y = {}/{}/{} -> {}", key.zoom(), key.tileX(), key.tileY(), tilePath);
            return decodeTile(downloaded);
        }
        catch (IOException exception)
        {
            terracraft.LOGGER.warn("Failed to load DEM tile {}/{}/{}: {}", key.zoom(), key.tileX(), key.tileY(), exception.toString());
            return null;
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            terracraft.LOGGER.warn("Interrupted while loading DEM tile {}/{}/{}", key.zoom(), key.tileX(), key.tileY());
            return null;
        }
    }

    private BufferedImage downloadTile(TileKey key) throws IOException, InterruptedException
    {
        String url = urlTemplate.formatted(key.zoom(), key.tileX(), key.tileY());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Terracraft/0.1.0 (Minecraft Forge mod)")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200)
        {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }

        try (InputStream body = response.body())
        {
            BufferedImage image = ImageIO.read(body);
            if (image == null)
            {
                throw new IOException("ImageIO returned null for " + url);
            }
            return image;
        }
    }

    private static double[][] decodeTile(BufferedImage image)
    {
        double[][] heights = new double[256][256];
        for (int x = 0; x < 256; x++)
        {
            for (int y = 0; y < 256; y++)
            {
                heights[x][y] = TerrariumTileMath.decodeElevationMeters(image.getRGB(x, y));
            }
        }
        return heights;
    }

    private static double[][] flatTile()
    {
        double[][] heights = new double[256][256];
        for (int x = 0; x < 256; x++)
        {
            for (int y = 0; y < 256; y++)
            {
                heights[x][y] = 0.0D;
            }
        }
        return heights;
    }

    public void shutdown()
    {
        downloadExecutor.shutdownNow();
    }
}
