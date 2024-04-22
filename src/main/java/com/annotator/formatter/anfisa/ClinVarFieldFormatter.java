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
            put("ClinVar_Submitters", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonObject clinVarObject = variant.getJsonObject("ClinVar");
                if (clinVarObject.isEmpty()) {
                    return "";
                }

                JsonArray significances = clinVarObject.getJsonArray("Significances");
                JsonArray submitters = new JsonArray(significances.stream().map((Object significanceEntry) -> ((JsonObject) significanceEntry).getJsonObject("Submitter")).toList());

                return String.valueOf(submitters);
            });
            put("Number_of_clinvar_submitters", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonObject clinVarObject = variant.getJsonObject("ClinVar");
                if (clinVarObject.isEmpty()) {
                    return "";
                }

                int numberOfSubmitters = clinVarObject.getJsonArray("Significances").size();

                return Integer.toString(numberOfSubmitters);
            });
            put("ReviewStatus", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonObject clinVarObject = variant.getJsonObject("ClinVar");
                if (clinVarObject.isEmpty()) {
                    return "";
                }

                return clinVarObject.getString("ReviewStatus");
            });
            put("Clinvar_criteria_provided", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonObject clinVarObject = variant.getJsonObject("ClinVar");
                if (clinVarObject.isEmpty()) {
                    return "";
                }

                return clinVarObject.getString("ReviewStatus").contains(REVIEW_STATUS_CRITERIA_PROVIDED) ? "true" : "false";
            });
            put("Clinvar_conflicts", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonObject clinVarObject = variant.getJsonObject("ClinVar");
                if (clinVarObject.isEmpty()) {
                    return "";
                }

                return clinVarObject.getString("ReviewStatus").contains(REVIEW_STATUS_NO_CONFLICTS) ? "false" : "true";
            });
            put("Clinvar_acmg_guidelines", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonObject clinVarObject = variant.getJsonObject("ClinVar");
                if (clinVarObject.isEmpty()) {
                    return "";
                }

                return clinVarObject.getString("Guidelines");
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(aStorageClinVarKeyMap, anfisaJson, variant);
    }
}
