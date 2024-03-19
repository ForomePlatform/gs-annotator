package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface GnomADFieldFormatter {
    Map<String, Object> ASTORAGE_GNOMAD_KEY_MAP = new HashMap<>() {{
        put("gnomAD_AF", new String[]{
                "GnomAD", "AF"
        });
        put("gnomAD_AF_Exomes", (Function<JsonObject, String>) (JsonObject variant) -> {
            String sourceType = Formatter.extractValueFromAStorage(variant, new String[]{"GnomAD", "SOURCE"}, 0);
            if (sourceType.equals("e")) {
                return Formatter.extractValueFromAStorage(variant, new String[]{"GnomAD", "AF"}, 0);
            }

            return "";
        });
        put("gnomAD_AF_Genomes", (Function<JsonObject, String>) (JsonObject variant) -> {
            String sourceType = Formatter.extractValueFromAStorage(variant, new String[]{"GnomAD", "SOURCE"}, 0);
            if (sourceType.equals("g")) {
                return Formatter.extractValueFromAStorage(variant, new String[]{"GnomAD", "AF"}, 0);
            }

            return "";
        });
    }};
}
