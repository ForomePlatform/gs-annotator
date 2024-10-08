package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import com.annotator.utils.variant.Variant;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class SpliceAIFieldFormatter implements Formatter {
    private final JsonObject anfisaJson;
    private final Variant variant;
    private final Map<String, Object> aStorageSpliceAIKeyMap;
    private final Map<String, Object> preprocessedData;

    public SpliceAIFieldFormatter(Variant variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.preprocessedData = preprocessedData;
        this.anfisaJson = anfisaJson;
        this.aStorageSpliceAIKeyMap = getAStorageSpliceAIKeyMap();
        this.preprocessData();
    }

    private String getSpliceAlteringCategorization(float dsMaxScore) {
        if (dsMaxScore < 0.2) {
            return "unlikely";
        } else if (dsMaxScore < 0.5) {
            return "likely_pathogenic";
        } else if (dsMaxScore < 0.8) {
            return "pathogenic";
        } else {
            return "high_precision_pathogenic";
        }
    }

    private void preprocessData() {
        JsonArray spliceAIArray = variant.getVariantJson().getJsonArray("SpliceAI");
        if (spliceAIArray.isEmpty()) {
            return;
        }

        JsonObject spliceAIObject = spliceAIArray.getJsonObject(0);

        float maxDs = Float.parseFloat(spliceAIObject.getString("MAX_DS"));
        this.preprocessedData.put("spliceAIDsMax", maxDs);
    }

    private Map<String, Object> getAStorageSpliceAIKeyMap() {
        return new HashMap<>() {{
            put("splice_altering", (Supplier<String>) () -> {
                Float dsMax = (Float) preprocessedData.get("spliceAIDsMax");
                if (dsMax == null) {
                    return null;
                }

                return getSpliceAlteringCategorization(dsMax);
            });
            put("splice_ai_dsmax", (Supplier<Float>) () -> (Float) preprocessedData.get("spliceAIDsMax"));
        }};
    }

    public void formatData() {
        Formatter.formatData(aStorageSpliceAIKeyMap, anfisaJson, variant);
    }
}
