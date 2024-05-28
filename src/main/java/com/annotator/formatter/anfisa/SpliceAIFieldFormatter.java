package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class SpliceAIFieldFormatter implements Formatter {
    private final JsonObject anfisaJson;
    private final JsonObject variant;
    private final Map<String, Object> aStorageSpliceAIKeyMap;

    public SpliceAIFieldFormatter(JsonObject variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.anfisaJson = anfisaJson;
        this.aStorageSpliceAIKeyMap = getAStorageClinVarKeyMap();
        this.preprocessData();
    }

    private void preprocessData() {
    }

    private Map<String, Object> getAStorageClinVarKeyMap() {
        return new HashMap<>() {{
            put("splice_altering", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray spliceAIArray = variant.getJsonArray("SpliceAI");
                if (spliceAIArray.isEmpty()) {
                    return null;
                }

                JsonObject spliceAIObject = spliceAIArray.getJsonObject(0);

                return spliceAIObject.getString("DP_AL");
            });
            put("splice_ai_dsmax", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                JsonArray spliceAIArray = variant.getJsonArray("SpliceAI");
                if (spliceAIArray.isEmpty()) {
                    return null;
                }

                JsonObject spliceAIObject = spliceAIArray.getJsonObject(0);

                int dsAg = Integer.parseInt(spliceAIObject.getString("DS_AG"));
                int dsDg = Integer.parseInt(spliceAIObject.getString("DS_DG"));
                int dsAl = Integer.parseInt(spliceAIObject.getString("DS_AL"));
                int dsDl = Integer.parseInt(spliceAIObject.getString("DS_DL"));

                return Collections.max(Arrays.asList(dsAg, dsDg, dsAl, dsDl));
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(aStorageSpliceAIKeyMap, anfisaJson, variant);
    }
}
