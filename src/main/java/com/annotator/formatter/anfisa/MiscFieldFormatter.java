package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import com.annotator.utils.formatter.anfisa.AnfisaConstants;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MiscFieldFormatter implements Formatter {
    private final JsonObject variant;
    private final Map<String, Object> preprocessedData;
    private final JsonObject anfisaJson;
    private final Map<String, Object> miscKeyMap;

    public MiscFieldFormatter(JsonObject variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.preprocessedData = preprocessedData;
        this.anfisaJson = anfisaJson;
        this.miscKeyMap = getMiscKeyMap();
    }

    private Map<String, Object> getMiscKeyMap() {
        return new HashMap<>() {{
            put("zygosity", (Supplier<JsonArray>) () -> {
                Integer[] mappedGt = (Integer[]) preprocessedData.get(AnfisaConstants.MAPPED_GT_PREPROCESSED_DATA_MAP_KEY);
                if (mappedGt == null) {
                    return null;
                }

                return new JsonArray(Arrays.stream(mappedGt).toList());
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(miscKeyMap, anfisaJson, variant);
    }
}
