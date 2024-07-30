package com.annotator.formatter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Formatter {
    void formatData();

    static void formatData(Map<String, Object> anfisaToAStorageMap, JsonObject anfisaJson, JsonObject variant) {
        for (String key : anfisaToAStorageMap.keySet()) {
            Object valueFinder = anfisaToAStorageMap.get(key);

            if (valueFinder instanceof String[] aStorageKeyArray) {
                String value = Formatter.extractValueFromAStorage(variant, aStorageKeyArray, 0);
                anfisaJson.put(key, value);
            } else if (valueFinder instanceof Function) {
                Function<JsonObject, Object> valueFinderFunction = (Function<JsonObject, Object>) valueFinder;
                Object value = valueFinderFunction.apply(variant);
                anfisaJson.put(key, value);
            } else if (valueFinder instanceof Supplier) {
                Supplier<Object> valueFinderSupplier = (Supplier<Object>) valueFinder;
                Object value = valueFinderSupplier.get();
                anfisaJson.put(key, value);
            }
        }
    }

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
        System.out.println("\"");

        return null;
    }
}
