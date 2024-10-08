package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import com.annotator.utils.variant.Variant;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class DbNSFPFieldFormatter implements Formatter {
    private final JsonObject anfisaJson;
    private final Variant variant;
    private final Map<String, Object> aStorageDbNSFPKeyMap;
    private final Map<String, Object> preprocessedData;

    public DbNSFPFieldFormatter(Variant variant, Map<String, Object> preprocessedData, JsonObject anfisaJson) {
        this.variant = variant;
        this.preprocessedData = preprocessedData;
        this.anfisaJson = anfisaJson;
        this.aStorageDbNSFPKeyMap = getAStorageDbNSFPKeyMap();
        this.preprocessData();
    }

    private void preprocessData() {
        JsonArray dbNSFPArray = variant.getVariantJson().getJsonArray("DbNSFP");
        if (dbNSFPArray.isEmpty()) {
            return;
        }

        preprocessedData.put("dbNSFPTranscripts", new JsonArray());
        JsonArray transcripts = (JsonArray) preprocessedData.get("dbNSFPTranscripts");

        JsonObject dbNSFPObject = dbNSFPArray.getJsonObject(0);
        JsonArray dbNSFPVariants = dbNSFPObject.getJsonArray("variants");
        dbNSFPVariants.forEach((Object dbNSFPVariant) -> {
            JsonArray dbNSFPFacets = ((JsonObject) dbNSFPVariant).getJsonArray("facets");

            dbNSFPFacets.forEach((Object dbNSFPFacet) -> {
                JsonArray facetTranscripts = ((JsonObject) dbNSFPFacet).getJsonArray("transcripts");
                for (int i = 0; i < facetTranscripts.size(); i++) {
                    JsonObject newTranscript = new JsonObject(facetTranscripts.getValue(i).toString());
                    newTranscript.put("order_id", i);
                    transcripts.add(newTranscript);
                }
            });
        });
    }

    private Map<String, Object> getAStorageDbNSFPKeyMap() {
        return new HashMap<>() {{
            put("transcripts", (Function<Variant, JsonArray>) (Variant variant) -> {
                JsonArray dbNSFPArray = variant.getVariantJson().getJsonArray("DbNSFP");
                if (dbNSFPArray.isEmpty()) {
                    return null;
                }

                return (JsonArray) preprocessedData.get("dbNSFPTranscripts");
            });
        }};
    }

    public void formatData() {
        Formatter.formatData(aStorageDbNSFPKeyMap, anfisaJson, variant);
    }
}
