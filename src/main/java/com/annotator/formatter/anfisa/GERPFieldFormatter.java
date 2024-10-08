package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import com.annotator.utils.variant.Variant;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GERPFieldFormatter implements Formatter {
    private final JsonObject anfisaJson;
    private final Variant variant;
    private final Map<String, Object> gerpKeyMap;
    private final Map<String, Object> preprocessedData;

    public GERPFieldFormatter(Variant variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.anfisaJson = anfisaJson;
        this.gerpKeyMap = getGerpKeyMap();
        this.preprocessedData = preprocessedData;
        this.preprocessData();
    }

    private void preprocessData() {
        JsonArray gerpArray = variant.getVariantJson().getJsonArray("GERP");
        preprocessedData.put("gerpArray", gerpArray);
    }

    private Map<String, Object> getGerpKeyMap() {
        return new HashMap<>() {{
            put("gerp_score", (Supplier<Double>) () -> {
                JsonArray gerpArray = (JsonArray) preprocessedData.get("gerpArray");
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
