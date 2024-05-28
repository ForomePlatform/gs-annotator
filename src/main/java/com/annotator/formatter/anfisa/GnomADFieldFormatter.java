package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class GnomADFieldFormatter implements Formatter {
    public static final List<String> INBREAD_POPULATION_KEYS = List.of(new String[]{"asj", "fin"});
    private final Map<String, Object> preprocessedData;
    private final JsonObject anfisaJson;
    private final JsonObject variant;
    private final Map<String, Object> aStorageGnomADKeyMap;

    public GnomADFieldFormatter(JsonObject variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.preprocessedData = preprocessedData;
        this.anfisaJson = anfisaJson;
        this.aStorageGnomADKeyMap = getAStorageGnomADKeyMap();
        this.preprocessData();
    }

    private void preprocessData() {
        JsonArray gnomADArray = variant.getJsonArray("GnomAD");
        preprocessedData.put("gnomADArray", gnomADArray);

        Map<String, Integer> populationAFMap = new HashMap<>();
        if (!gnomADArray.isEmpty()) {
            JsonObject gnomADVariant = gnomADArray.getJsonObject(0);
            Iterator<Map.Entry<String, Object>> variantEntryIterator = gnomADVariant.stream().iterator();
            while (variantEntryIterator.hasNext()) {
                Map.Entry<String, Object> variantEntry = variantEntryIterator.next();
                if (variantEntry.getValue() instanceof JsonObject) {
                    String population = variantEntry.getKey();
                    int populationAC = Integer.parseInt(
                            ((JsonObject) variantEntry.getValue()).getString("AC_" + population)
                    );
                    int populationAN = Integer.parseInt(
                            ((JsonObject) variantEntry.getValue()).getString("AN_" + population)
                    );

                    // Assuming exome record(second element in the array) contains all the population fields that genome does
                    if (gnomADArray.size() == 2) {
                        populationAC += Integer.parseInt(gnomADArray.getJsonObject(1)
                                .getJsonObject(population).getString("AC_" + population));
                        populationAN += Integer.parseInt(gnomADArray.getJsonObject(1)
                                .getJsonObject(population).getString("AN_" + population));
                    }

                    int populationAF = populationAN == 0 ? 0 : populationAC / populationAN;

                    populationAFMap.put(population, populationAF);
                }
            }
        }

        preprocessedData.put("populationAFMap", populationAFMap);
    }

    private Map<String, Object> getAStorageGnomADKeyMap() {
        return new HashMap<>() {{
            put("gnomad_af", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");
                if (gnomADArray.isEmpty()) {
                    return null;
                }

                int alleleCount = Integer.parseInt(gnomADArray.getJsonObject(0).getString("AC"));
                int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getString("AN"));

                if (gnomADArray.size() == 2) {
                    alleleCount += Integer.parseInt(gnomADArray.getJsonObject(1).getString("AC"));
                    alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getString("AN"));
                }

                if (alleleNumber == 0) {
                    return null;
                }

                return alleleCount / alleleNumber;
            });
            put("gnomad_af_exomes", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");
                for (Object gnomADVariant : gnomADArray) {
                    if (((JsonObject) gnomADVariant).getString("SOURCE").equals("e")) {
                        return ((JsonObject) gnomADVariant).getString("AF");
                    }
                }

                return null;
            });
            put("gnomad_af_genomes", (Function<JsonObject, String>) (JsonObject variant) -> {
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");
                for (Object gnomADVariant : gnomADArray) {
                    if (((JsonObject) gnomADVariant).getString("SOURCE").equals("g")) {
                        return ((JsonObject) gnomADVariant).getString("AF");
                    }
                }

                return null;
            });
            put("gnomad_popmax", (Function<JsonObject, String>) (JsonObject variant) -> {
                Map<String, Integer> populationAFMap = (Map<String, Integer>) preprocessedData.get("populationAFMap");
                Map.Entry<String, Integer> maxEntry = null;

                for (Map.Entry<String, Integer> entry : populationAFMap.entrySet()) {
                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                        maxEntry = entry;
                    }
                }

                if (maxEntry == null) {
                    return null;
                }

                return maxEntry.getKey();
            });
            put("gnomad_popmax_af", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) aStorageGnomADKeyMap.get("gnomad_popmax");
                String popMax = anfisaJson.containsKey("gnomad_popmax") ? anfisaJson.getString("gnomad_popmax") : getPopMax.apply(variant);
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

                if (gnomADArray.isEmpty()) {
                    return null;
                }

                int alleleCount = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AC_" + popMax));
                int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN_" + popMax));

                if (gnomADArray.size() == 2) {
                    alleleCount += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AC_" + popMax));
                    alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN_" + popMax));
                }

                if (alleleNumber == 0) {
                    return null;
                }

                return alleleCount / alleleNumber;
            });
            put("gnomad_popmax_an", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) aStorageGnomADKeyMap.get("gnomad_popmax");
                String popMax = anfisaJson.containsKey("gnomad_popmax") ? anfisaJson.getString("gnomad_popmax") : getPopMax.apply(variant);
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

                if (gnomADArray.isEmpty()) {
                    return null;
                }

                int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN_" + popMax));

                if (gnomADArray.size() == 2) {
                    alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN_" + popMax));
                }

                return alleleNumber;
            });
            put("gnomad_popmax_outbred", (Function<JsonObject, String>) (JsonObject variant) -> {
                Map<String, Integer> populationAFMap = (Map<String, Integer>) preprocessedData.get("populationAFMap");
                Map.Entry<String, Integer> maxEntry = null;
                for (Map.Entry<String, Integer> entry : populationAFMap.entrySet()) {
                    if (INBREAD_POPULATION_KEYS.contains(entry.getKey())) continue;

                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                        maxEntry = entry;
                    }
                }

                if (maxEntry == null) {
                    return null;
                }

                return maxEntry.getKey();
            });
            put("gnomad_popmax_af_outbred", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) aStorageGnomADKeyMap.get("gnomad_popmax_outbred");
                String popMax = anfisaJson.containsKey("gnomad_popmax_outbred") ? anfisaJson.getString("gnomad_popmax_outbred") : getPopMax.apply(variant);
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

                if (gnomADArray.isEmpty()) {
                    return null;
                }

                int alleleCount = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AC_" + popMax));
                int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN_" + popMax));

                if (gnomADArray.size() == 2) {
                    alleleCount += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AC_" + popMax));
                    alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN_" + popMax));
                }

                if (alleleNumber == 0) {
                    return null;
                }

                return alleleCount / alleleNumber;
            });
            put("gnomad_popmax_an_outbred", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) aStorageGnomADKeyMap.get("gnomad_popmax_outbred");
                String popMax = anfisaJson.containsKey("gnomad_popmax_outbred") ? anfisaJson.getString("gnomad_popmax_outbred") : getPopMax.apply(variant);
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

                if (gnomADArray.isEmpty()) {
                    return null;
                }

                int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN_" + popMax));

                if (gnomADArray.size() == 2) {
                    alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN_" + popMax));
                }

                return alleleNumber;
            });
            put("gnomad_hom", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

                if (gnomADArray.isEmpty()) {
                    return null;
                }

                int nhomalt = Integer.parseInt(gnomADArray.getJsonObject(0).getString("nhomalt"));

                if (gnomADArray.size() == 2) {
                    nhomalt += Integer.parseInt(gnomADArray.getJsonObject(1).getString("nhomalt"));
                }

                return nhomalt;
            });
            put("gnomad_hem", (Function<JsonObject, Integer>) (JsonObject variant) -> {
                JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

                if (gnomADArray.isEmpty()) {
                    return null;
                }

                int nhomaltXY = Integer.parseInt(gnomADArray.getJsonObject(0).getString("nhomalt_XY"));

                if (gnomADArray.size() == 2) {
                    nhomaltXY += Integer.parseInt(gnomADArray.getJsonObject(1).getString("nhomalt_XY"));
                }

                return nhomaltXY;
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(aStorageGnomADKeyMap, anfisaJson, variant);
    }
}
