package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ClinVarFieldFormatter implements Formatter {
    public static final String REVIEW_STATUS_CRITERIA_PROVIDED = "criteria provided";
    public static final String REVIEW_STATUS_NO_CONFLICTS = "no conflicts";
    public static final List<String> BENIGN_SIGNIFICANCES = new ArrayList<>() {{
        add("Benign");
        add("Likely benign");
    }};
    public static final List<String> TRUSTED_SUBMITTERS = new ArrayList<>() {{
        add("Laboratory for Molecular Medicine, Partners HealthCare Personalized Medicine: \"LMM\"");
        add("GeneDx");
        add("Invitae");
    }};
    // For RCV (variation-condition) records
    public static final Map<String, String> REVIEW_STATUS_TO_STARS = new HashMap<>() {{
        put("practice guideline", "four");
        put("reviewed by expert panel", "three");
        put("criteria provided, multiple submitters, no conflicts", "two");
        put("criteria provided, conflicting classifications", "one");
        put("criteria provided, single submitter", "one");
        put("no assertion criteria provided", "none");
        put("no classification provided", "none");
        put("no classification for the individual variant", "none");
    }};
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
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);
                JsonArray significances = clinVarObject.getJsonArray("Significances");

                return new JsonArray(significances.stream().map((Object significanceEntry) -> ((JsonObject) significanceEntry).getJsonObject("Submitter")).toList());
            });
            put("Clinvar_Benign", (Function<JsonObject, Boolean>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);
                String clinicalSignificance = clinVarObject.getString("ClinicalSignificance");

                return BENIGN_SIGNIFICANCES.contains(clinicalSignificance);
            });
            put("ClinVar_Significance", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);
                return clinVarObject.getString("ClinicalSignificance");
            });
            put("Clinvar_Trusted_Significance", (Function<JsonObject, JsonArray>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);
                JsonArray significances = clinVarObject.getJsonArray("Significances");

                return new JsonArray(significances.stream()
                        .filter((Object significanceEntry) ->
                                TRUSTED_SUBMITTERS.contains(
                                        ((JsonObject) significanceEntry).getJsonObject("Submitter")
                                                .getString("SubmitterName")))
                        .map((Object significanceEntry) ->
                                ((JsonObject) significanceEntry).getString("ClinicalSignificance"))
                        .toList());
            });
            put("Clinvar_stars", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);
                String reviewStatus = clinVarObject.getString("ReviewStatus");

                return REVIEW_STATUS_TO_STARS.get(reviewStatus);
            });
            put("Number_of_clinvar_submitters", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
                }

                JsonObject clinVarObject = clinVarArray.getJsonObject(0);

                return clinVarObject.getJsonArray("Significances").size();
            });
            put("Clinvar_review_status", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray clinVarArray = variant.getJsonArray("ClinVar");
                if (clinVarArray.isEmpty()) {
                    return null;
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
                    return null;
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
