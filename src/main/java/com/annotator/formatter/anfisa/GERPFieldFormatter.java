package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class GERPFieldFormatter implements Formatter {
    private final JsonObject anfisaJson;
    private final JsonObject variant;
    private final Map<String, Object> gerpKeyMap;

    public GERPFieldFormatter(JsonObject variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.anfisaJson = anfisaJson;
        this.gerpKeyMap = getGerpKeyMap();
        this.preprocessData();
    }

    private void preprocessData() {
    }

    private Map<String, Object> getGerpKeyMap() {
        return new HashMap<>() {{
            put("gerp_score", (Function<JsonObject, Double>) (JsonObject variant) -> {
                JsonArray gerpArray = variant.getJsonArray("GERP");
                if (gerpArray.isEmpty()) {
                    return null;
                }

                JsonArray gerpObject = gerpArray.getJsonArray(0);

                return Double.valueOf(gerpObject.getString(1));
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(gerpKeyMap, anfisaJson, variant);
    }
}
