package com.annotator.formatter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public interface Formatter {
    Map<String, Object> preprocessedData = new HashMap<>();
    JsonObject anfisaJson = new JsonObject();

    JsonObject extractData();

    void preprocessData(JsonObject variant);

    static String extractValueFromAStorage(Object currentObject, String[] aStorageKeyArray, int currentIndex) {
        Object result = extractObjectFromAStorage(currentObject, aStorageKeyArray, currentIndex);
        if (result instanceof String) {
            return (String) result;
        }

        return null;
    }

    static Object extractObjectFromAStorage(Object currentObject, String[] aStorageKeyArray, int currentIndex) {
        if (currentIndex == aStorageKeyArray.length - 1) {
            return currentObject;
        }

        if (currentIndex < aStorageKeyArray.length) {
            String aStorageKey = aStorageKeyArray[currentIndex];

            if (currentObject instanceof JsonObject && ((JsonObject) currentObject).containsKey(aStorageKey)) {
                Object value = ((JsonObject) currentObject).getValue(aStorageKey);
                extractObjectFromAStorage(value, aStorageKeyArray, currentIndex + 1);
            } else if (currentIndex + 1 < aStorageKeyArray.length && currentObject instanceof JsonArray) {
                try {
                    int arrayIndex = Integer.parseInt(aStorageKeyArray[currentIndex + 1]);
                    extractObjectFromAStorage(((JsonArray) currentObject).getValue(arrayIndex), aStorageKeyArray, currentIndex + 1);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        System.out.print("Unsupported JSON structure for key \"");
        System.out.print(aStorageKeyArray[currentIndex]);
        return null;
    }
}
