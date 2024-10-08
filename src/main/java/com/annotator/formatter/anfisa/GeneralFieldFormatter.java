package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import com.annotator.utils.formatter.anfisa.AnfisaConstants;
import com.annotator.utils.variant.Variant;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GeneralFieldFormatter implements Formatter {
    private final Variant variant;
    private final Map<String, Object> preprocessedData;
    private final JsonObject anfisaJson;
    private final Map<String, Object> miscKeyMap;

    public GeneralFieldFormatter(Variant variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.preprocessedData = preprocessedData;
        this.anfisaJson = anfisaJson;
        this.miscKeyMap = getMiscKeyMap();
    }

    private Map<String, Object> getMiscKeyMap() {
        return new HashMap<>() {{
            put("chromosome", (Supplier<String>) variant::getChr);
            put("start", (Supplier<Integer>) variant::getStartPos);
            put("end", (Supplier<Integer>) variant::getEndPos);
            put("ref", (Supplier<String>) variant::getRef);
            put("alt", (Supplier<String>) variant::getAlt);
            put("color", (Supplier<String>) variant::getColor);
            put("genes", (Supplier<String>) variant::getGenes);
            put("label", (Supplier<String>) variant::getLabel);
            put("zygosity", (Supplier<JsonArray>) () -> {
                List<Integer> mappedGt = (List<Integer>) preprocessedData.get(AnfisaConstants.MAPPED_GT_PREPROCESSED_DATA_MAP_KEY);
                if (mappedGt == null) {
                    return null;
                }

                return new JsonArray(mappedGt);
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(miscKeyMap, anfisaJson, variant);
    }
}
