package com.torr.terracraft.geo.ecoregion;

import com.torr.terracraft.geo.TerrariumTileMath;
import com.torr.terracraft.terracraft;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class EcoregionTileCache
{
    private record TileKey(int zoom, int tileX, int tileY)
    {
    }

    private final Path cacheRoot;
    private final int zoom;
    private final WwfEcoregionDataset dataset;
    private final ExecutorService rasterExecutor;
    private final Map<TileKey, int[][]> memoryCache = new ConcurrentHashMap<>();
    private final Map<TileKey, CompletableFuture<int[][]>> inFlight = new ConcurrentHashMap<>();

    public EcoregionTileCache(Path cacheRoot, int zoom, WwfEcoregionDataset dataset)
    {
        this.cacheRoot = cacheRoot;
        this.zoom = zoom;
        this.dataset = dataset;
        this.rasterExecutor = Executors.newFixedThreadPool(2, runnable ->
        {
            Thread thread = new Thread(runnable, "Terracraft-Ecoregion");
            thread.setDaemon(true);
            return thread;
        });
    }

    public EcoregionInfo sample(double latitude, double longitude)
    {
        TerrariumTileMath.TileSample sample = TerrariumTileMath.locate(zoom, latitude, longitude);
        int[][] ecoIds = getTileEcoIds(sample.tileX(), sample.tileY());
        int ecoId = ecoIds[sample.pixelX()][sample.pixelY()];
        if (ecoId == 0)
        {
            return EcoregionInfo.UNKNOWN;
        }
        return dataset.metadataForEcoId(ecoId);
    }

    public Path cacheRoot()
    {
        return cacheRoot;
    }

    public void prefetchArea(double minLat, double maxLat, double minLon, double maxLon)
    {
        TerrariumTileMath.TileRange range = TerrariumTileMath.tilesCovering(zoom, minLat, maxLat, minLon, maxLon);
        List<CompletableFuture<int[][]>> pending = new ArrayList<>();

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

    private int[][] getTileEcoIds(int tileX, int tileY)
    {
        TileKey key = new TileKey(zoom, tileX, tileY);
        int[][] cached = memoryCache.get(key);
        if (cached != null)
        {
            return cached;
        }

        return startTileLoad(key).join();
    }

    private CompletableFuture<int[][]> startTileLoad(TileKey key)
    {
        return inFlight.computeIfAbsent(key, ignored ->
        {
            CompletableFuture<int[][]> pending = CompletableFuture.supplyAsync(
                    () -> loadTileEcoIds(key),
                    rasterExecutor
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

    private int[][] loadTileEcoIds(TileKey key)
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
            BufferedImage rasterized = rasterizeTile(key);
            ImageIO.write(rasterized, "png", tilePath.toFile());
            terracraft.LOGGER.info("Cached ecoregion tile z/x/y = {}/{}/{} -> {}", key.zoom(), key.tileX(), key.tileY(), tilePath);
            return decodeTile(rasterized);
        }
        catch (IOException exception)
        {
            terracraft.LOGGER.warn("Failed to load ecoregion tile {}/{}/{}: {}", key.zoom(), key.tileX(), key.tileY(), exception.toString());
            return emptyTile();
        }
    }

    private BufferedImage rasterizeTile(TileKey key)
    {
        TerrariumTileMath.TileBounds bounds = TerrariumTileMath.tileBounds(key.zoom(), key.tileX(), key.tileY());
        List<WwfEcoregionFeature> features = dataset.featuresInBounds(
                bounds.minLatitude(),
                bounds.maxLatitude(),
                bounds.minLongitude(),
                bounds.maxLongitude()
        );

        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (WwfEcoregionFeature feature : features)
        {
            int rgb = encodeEcoId(feature.ecoId());
            graphics.setColor(new java.awt.Color(rgb));
            for (List<List<double[]>> polygon : feature.polygons())
            {
                Path2D path = buildPath(polygon, key);
                if (path != null)
                {
                    graphics.fill(path);
                }
            }
        }

        graphics.dispose();
        return image;
    }

    private static Path2D buildPath(List<List<double[]>> polygonRings, TileKey key)
    {
        if (polygonRings.isEmpty())
        {
            return null;
        }

        Path2D path = new Path2D.Double();
        for (List<double[]> ring : polygonRings)
        {
            appendRing(path, ring, key);
        }
        return path;
    }

    private static void appendRing(Path2D path, List<double[]> ring, TileKey key)
    {
        if (ring.isEmpty())
        {
            return;
        }

        double[] first = ring.get(0);
        path.moveTo(
                TerrariumTileMath.longitudeToPixelX(first[0], key.zoom(), key.tileX()) + 0.5D,
                TerrariumTileMath.latitudeToPixelY(first[1], key.zoom(), key.tileY()) + 0.5D
        );

        for (int index = 1; index < ring.size(); index++)
        {
            double[] point = ring.get(index);
            path.lineTo(
                    TerrariumTileMath.longitudeToPixelX(point[0], key.zoom(), key.tileX()) + 0.5D,
                    TerrariumTileMath.latitudeToPixelY(point[1], key.zoom(), key.tileY()) + 0.5D
            );
        }

        path.closePath();
    }

    private static int[][] decodeTile(BufferedImage image)
    {
        int[][] ecoIds = new int[256][256];
        for (int x = 0; x < 256; x++)
        {
            for (int y = 0; y < 256; y++)
            {
                ecoIds[x][y] = decodeEcoId(image.getRGB(x, y));
            }
        }
        return ecoIds;
    }

    private static int[][] emptyTile()
    {
        return new int[256][256];
    }

    static int encodeEcoId(int ecoId)
    {
        int red = (ecoId >> 16) & 0xFF;
        int green = (ecoId >> 8) & 0xFF;
        int blue = ecoId & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }

    static int decodeEcoId(int rgb)
    {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }

    public void shutdown()
    {
        rasterExecutor.shutdownNow();
    }
}
