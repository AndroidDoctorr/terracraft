package com.torr.terracraft.geo;

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
        TerrariumTileMath.TileSample sample = TerrariumTileMath.locate(zoom, latitude, longitude);
        double[][] heights = getTileHeights(zoom, sample.tileX(), sample.tileY());
        return heights[sample.pixelX()][sample.pixelY()];
    }

    public Path cacheRoot()
    {
        return cacheRoot;
    }

    private double[][] getTileHeights(int zoom, int tileX, int tileY)
    {
        TileKey key = new TileKey(zoom, tileX, tileY);
        double[][] cached = memoryCache.get(key);
        if (cached != null)
        {
            return cached;
        }

        CompletableFuture<double[][]> pending = inFlight.computeIfAbsent(key, ignored ->
                CompletableFuture.supplyAsync(() -> loadTileHeights(key), downloadExecutor));

        try
        {
            double[][] loaded = pending.join();
            memoryCache.putIfAbsent(key, loaded);
            return loaded;
        }
        finally
        {
            inFlight.remove(key);
        }
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
            return flatTile();
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();
            terracraft.LOGGER.warn("Interrupted while loading DEM tile {}/{}/{}", key.zoom(), key.tileX(), key.tileY());
            return flatTile();
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
