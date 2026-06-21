package com.torr.terracraft.geo.ecoregion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.torr.terracraft.terracraft;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class WwfEcoregionDownloader
{
    private static final int PAGE_SIZE = 2000;

    private WwfEcoregionDownloader()
    {
    }

    public static void downloadIfMissing(Path destination, String queryUrlTemplate) throws IOException, InterruptedException
    {
        if (Files.exists(destination) && Files.size(destination) > 0L)
        {
            terracraft.LOGGER.info("WWF ecoregion data already present at {}", destination.toAbsolutePath());
            return;
        }

        Files.createDirectories(destination.getParent());
        terracraft.LOGGER.info("Downloading WWF ecoregion GeoJSON to {} (this may take a minute)...", destination.toAbsolutePath());

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        JsonArray mergedFeatures = new JsonArray();
        int offset = 0;
        int pages = 0;

        while (true)
        {
            String pageUrl = queryUrlTemplate.formatted(offset);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pageUrl))
                    .timeout(Duration.ofMinutes(2))
                    .header("User-Agent", "Terracraft/0.3.0 (Minecraft Forge mod)")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200)
            {
                throw new IOException("HTTP " + response.statusCode() + " for " + pageUrl);
            }

            JsonObject page;
            try (InputStream body = response.body())
            {
                page = JsonParser.parseReader(new java.io.InputStreamReader(body, StandardCharsets.UTF_8)).getAsJsonObject();
            }

            JsonArray features = page.getAsJsonArray("features");
            if (features == null || features.isEmpty())
            {
                break;
            }

            for (JsonElement feature : features)
            {
                mergedFeatures.add(feature);
            }

            pages++;
            terracraft.LOGGER.info("Downloaded ecoregion page {} ({} features so far)", pages, mergedFeatures.size());

            if (features.size() < PAGE_SIZE)
            {
                break;
            }

            offset += PAGE_SIZE;
        }

        if (mergedFeatures.isEmpty())
        {
            throw new IOException("ArcGIS query returned no ecoregion features");
        }

        JsonObject collection = new JsonObject();
        collection.addProperty("type", "FeatureCollection");
        collection.add("features", mergedFeatures);
        Files.writeString(destination, collection.toString(), StandardCharsets.UTF_8);
        terracraft.LOGGER.info("Saved {} WWF ecoregion features to {}", mergedFeatures.size(), destination.toAbsolutePath());
    }
}
