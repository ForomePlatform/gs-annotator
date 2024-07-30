package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import com.annotator.utils.formatter.anfisa.AnfisaConstants;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Anfisa implements AnfisaConstants {
    public static final String[] ANFISA_FIELD_GROUPS = {
            "GnomAD",
            "ClinVar",
            "DbNSFP",
//            "SpliceAI" TODO: Few questions still unanswered...
//            "GERP" TODO: hg38 -> hg19 liftover needs to be implemented...
            "Misc"
    };

    private final JsonObject variant;
    private final Map<String, Object> preprocessedData;
    private final JsonObject anfisaJson;

    public Anfisa(JsonObject variant, JsonArray famJson, Integer[] mappedGt) {
        this.variant = variant;
        this.preprocessedData = new HashMap<>();
        this.preprocessedData.put(FAM_JSON_PREPROCESSED_DATA_MAP_KEY, famJson);
        this.preprocessedData.put(MAPPED_GT_PREPROCESSED_DATA_MAP_KEY, mappedGt);
        this.anfisaJson = new JsonObject();
    }

    public JsonObject extractData() throws Exception {
        for (String fieldGroupName : ANFISA_FIELD_GROUPS) {
            Class<?> cls = Class.forName("com.annotator.formatter.anfisa." + fieldGroupName + "FieldFormatter");
            Constructor<?> constructor = cls.getConstructor(
                    JsonObject.class,
                    Map.class,
                    JsonObject.class
            );

            Formatter formatter = (Formatter) constructor.newInstance(this.variant, this.preprocessedData, this.anfisaJson);
            formatter.formatData();
        }

        return anfisaJson;
    }
}
