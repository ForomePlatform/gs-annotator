package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface GnomADFieldFormatter extends Formatter {
    default void preprocessData(JsonObject variant) {
        JsonArray gnomADArray = variant.getJsonArray("GnomAD");
        preprocessedData.put("gnomADArray", gnomADArray);

        Map<String, Integer> populationAcMap = new HashMap<>();
        for (Object gnomADVariant : gnomADArray) {
            Iterator<Map.Entry<String, Object>> variantEntryIterator = ((JsonObject) gnomADVariant).stream().iterator();
            while (variantEntryIterator.hasNext()) {
                Map.Entry<String, Object> variantEntry = variantEntryIterator.next();
                if (variantEntry.getValue() instanceof JsonObject) {
                    String population = variantEntry.getKey();
                    String populationAC = ((JsonObject) variantEntry.getValue()).getString("AC_" + population);
                    if (populationAcMap.containsKey(population)) {
                        populationAcMap.put(population, Integer.parseInt(populationAC) + populationAcMap.get(population));
                    } else {
                        populationAcMap.put(population, Integer.parseInt(populationAC));
                    }
                }
            }
        }
        preprocessedData.put("populationAcMap", populationAcMap);
    }

    Map<String, Object> ASTORAGE_GNOMAD_KEY_MAP = new HashMap<>() {{
        put("gnomAD_AF", (Function<JsonObject, String>) (JsonObject variant) -> {
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");
            int alleleCount = Integer.parseInt(gnomADArray.getJsonObject(0).getString("AC"));
            int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getString("AN"));

            if (gnomADArray.size() == 2) {
                alleleCount += Integer.parseInt(gnomADArray.getJsonObject(1).getString("AC"));
                alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getString("AN"));
            }

            return Integer.toString(alleleCount / alleleNumber);
        });
        put("gnomAD_AF_Exomes", (Function<JsonObject, String>) (JsonObject variant) -> {
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");
            for (Object gnomADVariant : gnomADArray) {
                if (((JsonObject) gnomADVariant).getString("SOURCE").equals("e")) {
                    return ((JsonObject) gnomADVariant).getString("AF");
                }
            }

            return "";
        });
        put("gnomAD_AF_Genomes", (Function<JsonObject, String>) (JsonObject variant) -> {
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");
            for (Object gnomADVariant : gnomADArray) {
                if (((JsonObject) gnomADVariant).getString("SOURCE").equals("g")) {
                    return ((JsonObject) gnomADVariant).getString("AF");
                }
            }

            return "";
        });
        put("gnomAD_PopMax", (Function<JsonObject, String>) (JsonObject variant) -> {
            Map<String, Integer> populationAcMap = (Map<String, Integer>) preprocessedData.get("populationAcMap");
            Map.Entry<String, Integer> maxEntry = null;
            for (Map.Entry<String, Integer> entry : populationAcMap.entrySet()) {
                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                    maxEntry = entry;
                }
            }

            assert maxEntry != null;
            return maxEntry.getKey();
        });
        put("gnomAD_PopMax_AF", (Function<JsonObject, String>) (JsonObject variant) -> {
            Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) ASTORAGE_GNOMAD_KEY_MAP.get("gnomAD_PopMax");
            String popMax = anfisaJson.containsKey("gnomAD_PopMax") ? anfisaJson.getString("gnomAD_PopMax") : getPopMax.apply(variant);
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

            int alleleCount = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AC" + popMax));
            int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN" + popMax));
            if (gnomADArray.size() == 2) {
                alleleCount += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AC" + popMax));
                alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN" + popMax));
            }

            return Integer.toString(alleleCount / alleleNumber);
        });
        put("gnomAD_PopMax_AN", (Function<JsonObject, String>) (JsonObject variant) -> {
            Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) ASTORAGE_GNOMAD_KEY_MAP.get("gnomAD_PopMax");
            String popMax = anfisaJson.containsKey("gnomAD_PopMax") ? anfisaJson.getString("gnomAD_PopMax") : getPopMax.apply(variant);
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

            int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN" + popMax));
            if (gnomADArray.size() == 2) {
                alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN" + popMax));
            }

            return Integer.toString(alleleNumber);
        });
        put("gnomAD_PopMax_Outbred", (Function<JsonObject, String>) (JsonObject variant) -> {
            Map<String, Integer> populationAcMap = (Map<String, Integer>) preprocessedData.get("populationAcMap");
            List<String> inbreadPopulations = List.of(new String[]{"asj", "fin"});
            Map.Entry<String, Integer> maxEntry = null;
            for (Map.Entry<String, Integer> entry : populationAcMap.entrySet()) {
                if (inbreadPopulations.contains(entry.getKey())) continue;

                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                    maxEntry = entry;
                }
            }

            assert maxEntry != null;
            return maxEntry.getKey();
        });
        put("gnomAD_PopMax_AF_Outbred", (Function<JsonObject, String>) (JsonObject variant) -> {
            Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) ASTORAGE_GNOMAD_KEY_MAP.get("gnomAD_PopMax_Outbred");
            String popMax = anfisaJson.containsKey("gnomAD_PopMax_Outbred") ? anfisaJson.getString("gnomAD_PopMax_Outbred") : getPopMax.apply(variant);
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

            int alleleCount = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AC" + popMax));
            int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN" + popMax));
            if (gnomADArray.size() == 2) {
                alleleCount += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AC" + popMax));
                alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN" + popMax));
            }

            return Integer.toString(alleleCount / alleleNumber);
        });
        put("gnomAD_PopMax_AN_Outbred", (Function<JsonObject, String>) (JsonObject variant) -> {
            Function<JsonObject, String> getPopMax = (Function<JsonObject, String>) ASTORAGE_GNOMAD_KEY_MAP.get("gnomAD_PopMax_Outbred");
            String popMax = anfisaJson.containsKey("gnomAD_PopMax_Outbred") ? anfisaJson.getString("gnomAD_PopMax_Outbred") : getPopMax.apply(variant);
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

            int alleleNumber = Integer.parseInt(gnomADArray.getJsonObject(0).getJsonObject(popMax).getString("AN" + popMax));
            if (gnomADArray.size() == 2) {
                alleleNumber += Integer.parseInt(gnomADArray.getJsonObject(1).getJsonObject(popMax).getString("AN" + popMax));
            }

            return Integer.toString(alleleNumber);
        });
        put("gnomAD_Hom", (Function<JsonObject, String>) (JsonObject variant) -> {
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

            int nhomalt = Integer.parseInt(gnomADArray.getJsonObject(0).getString("nhomalt"));
            if (gnomADArray.size() == 2) {
                nhomalt += Integer.parseInt(gnomADArray.getJsonObject(1).getString("nhomalt"));
            }

            return Integer.toString(nhomalt);
        });
        put("gnomAD_Hem", (Function<JsonObject, String>) (JsonObject variant) -> {
            JsonArray gnomADArray = (JsonArray) preprocessedData.get("gnomADArray");

            int nhomaltXY = Integer.parseInt(gnomADArray.getJsonObject(0).getString("nhomalt_XY"));
            if (gnomADArray.size() == 2) {
                nhomaltXY += Integer.parseInt(gnomADArray.getJsonObject(1).getString("nhomalt_XY"));
            }

            return Integer.toString(nhomaltXY);
        });
    }};
}
