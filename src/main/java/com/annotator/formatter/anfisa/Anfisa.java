package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Anfisa implements Formatter, DbNSFPFieldFormatter, GnomADFieldFormatter {
    private static final Map<String, Object> ASTORAGE_KEY_MAP = new HashMap<>() {{
        putAll(ASTORAGE_DBNSFP_KEY_MAP);
        putAll(ASTORAGE_GNOMAD_KEY_MAP);
    }};

    private final JsonObject variant;

    public Anfisa(JsonObject variant) {
        this.variant = variant;
    }

    public JsonObject extractData() {
        JsonObject anfisaJson = new JsonObject();

        for (String key : ASTORAGE_KEY_MAP.keySet()) {
            Object valueFinder = ASTORAGE_KEY_MAP.get(key);
            if (valueFinder instanceof String[] astorageKeyArray) {
                String value = Formatter.extractValueFromAStorage(this.variant, astorageKeyArray, 0);
                anfisaJson.put(key, value);
            } else if (valueFinder instanceof Function) {
                Function<JsonObject, String> valueFinderFunction = (Function<JsonObject, String>) valueFinder;
                String value = valueFinderFunction.apply(this.variant);
                anfisaJson.put(key, value);
            }
        }

        return anfisaJson;
    }
}
