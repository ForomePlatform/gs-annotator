package com.annotator.utils.astorage;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AStorageClient {
    private final HttpClient client;
    private final String serverUrl;

    public AStorageClient(String serverPath) {
        client = HttpClient.newHttpClient();
        this.serverUrl = serverPath;
    }

    public JsonObject queryUniversalVariant(
            String refBuild,
            String chr,
            String pos,
            String ref,
            String alt
    ) {
        URI queryUri = AStorageHelper.createUniversalVariantQuery(serverUrl, refBuild, chr, pos, ref, alt);

        if (queryUri == null) {
            return null; // TODO: Handle...
        }

        try {
            HttpRequest aStorageRequest = HttpRequest.newBuilder().uri(queryUri).GET().build();
            HttpResponse<String> response = client.send(aStorageRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();

                return new JsonObject(responseBody);
            } else {
                return null; // TODO: Handle...
            }
        } catch (IOException | InterruptedException e) {
            return null; // TODO: Handle...
        }
    }

    public JsonObject normalizeVariant(
            String refBuild,
            String chr,
            String pos,
            String ref,
            String alt
    ) {
        URI queryUri = AStorageHelper.createNormalizerQuery(serverUrl, refBuild, chr, pos, ref, alt);

        if (queryUri == null) {
            return null; // TODO: Handle...
        }

        try {
            HttpRequest aStorageRequest = HttpRequest.newBuilder().uri(queryUri).GET().build();
            HttpResponse<String> response = client.send(aStorageRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                String responseBody = response.body();

                return new JsonObject(responseBody);
            } else {
                return null; // TODO: Handle...
            }
        } catch (IOException | InterruptedException e) {
            return null; // TODO: Handle...
        }
    }
}
