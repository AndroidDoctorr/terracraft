package com.torr.terracraft.geo.ecoregion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.torr.terracraft.terracraft;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WwfEcoregionDataset
{
    private final List<WwfEcoregionFeature> features;
    private final Map<Integer, EcoregionInfo> metadataByEcoId;

    private WwfEcoregionDataset(List<WwfEcoregionFeature> features, Map<Integer, EcoregionInfo> metadataByEcoId)
    {
        this.features = features;
        this.metadataByEcoId = metadataByEcoId;
    }

    public static WwfEcoregionDataset load(Path geoJsonPath) throws IOException
    {
        try (Reader reader = Files.newBufferedReader(geoJsonPath))
        {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray featureArray = root.getAsJsonArray("features");
            if (featureArray == null)
            {
                throw new IOException("GeoJSON missing features array: " + geoJsonPath);
            }

            List<WwfEcoregionFeature> features = new ArrayList<>(featureArray.size());
            Map<Integer, EcoregionInfo> metadata = new HashMap<>();

            for (JsonElement element : featureArray)
            {
                WwfEcoregionFeature feature = parseFeature(element.getAsJsonObject());
                if (feature == null)
                {
                    continue;
                }

                features.add(feature);
                metadata.putIfAbsent(feature.ecoId(), new EcoregionInfo(
                        feature.ecoId(),
                        feature.biomeCode(),
                        feature.g200Biome(),
                        feature.realm(),
                        feature.name()
                ));
            }

            features.sort((left, right) -> Double.compare(left.boundingArea(), right.boundingArea()));
            terracraft.LOGGER.info("Loaded {} WWF ecoregion polygons ({} unique ECO_ID values) from {}",
                    features.size(), metadata.size(), geoJsonPath.toAbsolutePath());
            return new WwfEcoregionDataset(Collections.unmodifiableList(features), Collections.unmodifiableMap(metadata));
        }
    }

    public List<WwfEcoregionFeature> featuresInBounds(double minLat, double maxLat, double minLon, double maxLon)
    {
        List<WwfEcoregionFeature> matches = new ArrayList<>();
        for (WwfEcoregionFeature feature : features)
        {
            if (feature.intersects(minLat, maxLat, minLon, maxLon))
            {
                matches.add(feature);
            }
        }
        return matches;
    }

    public EcoregionInfo metadataForEcoId(int ecoId)
    {
        return metadataByEcoId.getOrDefault(ecoId, EcoregionInfo.UNKNOWN);
    }

    public int uniqueEcoIdCount()
    {
        return metadataByEcoId.size();
    }

    private static WwfEcoregionFeature parseFeature(JsonObject featureObject)
    {
        JsonObject properties = featureObject.getAsJsonObject("properties");
        JsonObject geometry = featureObject.getAsJsonObject("geometry");
        if (properties == null || geometry == null)
        {
            return null;
        }

        int ecoId = readInt(properties, "ECO_ID");
        if (ecoId == 0)
        {
            return null;
        }

        String name = readString(properties, "ECO_NAME");
        int biomeCode = readInt(properties, "BIOME");
        int g200Biome = readInt(properties, "G200_BIOME");
        String realm = readString(properties, "REALM");
        List<List<List<double[]>>> polygons = parseGeometry(geometry);
        if (polygons.isEmpty())
        {
            return null;
        }

        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;

        for (List<List<double[]>> polygon : polygons)
        {
            for (List<double[]> ring : polygon)
            {
                for (double[] point : ring)
                {
                    minLon = Math.min(minLon, point[0]);
                    maxLon = Math.max(maxLon, point[0]);
                    minLat = Math.min(minLat, point[1]);
                    maxLat = Math.max(maxLat, point[1]);
                }
            }
        }

        return new WwfEcoregionFeature(ecoId, name, biomeCode, g200Biome, realm, polygons, minLat, maxLat, minLon, maxLon);
    }

    private static List<List<List<double[]>>> parseGeometry(JsonObject geometry)
    {
        String type = geometry.get("type").getAsString();
        JsonArray coordinates = geometry.getAsJsonArray("coordinates");
        if (coordinates == null)
        {
            return List.of();
        }

        return switch (type)
        {
            case "Polygon" -> List.of(parseRings(coordinates));
            case "MultiPolygon" -> parseMultiPolygon(coordinates);
            default -> List.of();
        };
    }

    private static List<List<List<double[]>>> parseMultiPolygon(JsonArray polygons)
    {
        List<List<List<double[]>>> parsed = new ArrayList<>();
        for (JsonElement polygonElement : polygons)
        {
            parsed.add(parseRings(polygonElement.getAsJsonArray()));
        }
        return parsed;
    }

    private static List<List<double[]>> parseRings(JsonArray polygonCoordinates)
    {
        List<List<double[]>> rings = new ArrayList<>();
        for (JsonElement ringElement : polygonCoordinates)
        {
            JsonArray ringCoordinates = ringElement.getAsJsonArray();
            List<double[]> ring = new ArrayList<>(ringCoordinates.size());
            for (JsonElement pointElement : ringCoordinates)
            {
                JsonArray point = pointElement.getAsJsonArray();
                ring.add(new double[]{point.get(0).getAsDouble(), point.get(1).getAsDouble()});
            }
            if (!ring.isEmpty())
            {
                rings.add(ring);
            }
        }
        return rings;
    }

    private static int readInt(JsonObject object, String field)
    {
        JsonElement element = object.get(field);
        if (element == null || element.isJsonNull())
        {
            return 0;
        }
        return element.getAsInt();
    }

    private static String readString(JsonObject object, String field)
    {
        JsonElement element = object.get(field);
        if (element == null || element.isJsonNull())
        {
            return "";
        }
        return element.getAsString();
    }
}
