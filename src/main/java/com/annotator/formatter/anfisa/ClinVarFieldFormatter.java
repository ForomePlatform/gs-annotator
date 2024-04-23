package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ClinVarFieldFormatter implements Formatter {
    public static final String REVIEW_STATUS_CRITERIA_PROVIDED = "criteria provided";
    public static final String REVIEW_STATUS_NO_CONFLICTS = "no conflicts";
    private final JsonObject anfisaJson;
    private final JsonObject variant;
    private final Map<String, Object> aStorageClinVarKeyMap;

    public ClinVarFieldFormatter(JsonObject variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.anfisaJson = anfisaJson;
        this.aStorageClinVarKeyMap = getAStorageClinVarKeyMap();
        this.preprocessData();
    }

    private void preprocessData() {
    }

    private Map<String, Object> getAStorageClinVarKeyMap() {
        return new HashMap<>() {{
            put("ClinVar_Submitters", (Function<JsonObject, JsonArray>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return new JsonArray();
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);
                JsonArray significances = clinVarObject.getJsonArray("Significances");

				return new JsonArray(significances.stream().map((Object significanceEntry) -> ((JsonObject) significanceEntry).getJsonObject("Submitter")).toList());
            });
            put("Number_of_clinvar_submitters", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);

				return clinVarObject.getJsonArray("Significances").size();
            });
            put("ReviewStatus", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return "";
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);

                return clinVarObject.getString("ReviewStatus");
            });
            put("Clinvar_criteria_provided", (Function<JsonObject, Boolean>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);

                return clinVarObject.getString("ReviewStatus").contains(REVIEW_STATUS_CRITERIA_PROVIDED);
            });
            put("Clinvar_conflicts", (Function<JsonObject, Boolean>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);

                return !clinVarObject.getString("ReviewStatus").contains(REVIEW_STATUS_NO_CONFLICTS);
            });
            put("Clinvar_acmg_guidelines", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return "";
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);

                return clinVarObject.getString("Guidelines");
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(aStorageClinVarKeyMap, anfisaJson, variant);
    }
}
