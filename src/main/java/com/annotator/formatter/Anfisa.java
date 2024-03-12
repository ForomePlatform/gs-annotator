package com.annotator.formatter;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Anfisa implements Formatter {
	private static final Map<String, String[]> ASTORAGE_KEY_MAP = new HashMap<>() {{
		put("Transcript_id", new String[]{
			"DbNSFP", "variants", "facets", "transcripts", "Ensembl_transcriptid"
		});
		put("gnomAD_AF", new String[]{
			"GnomAD", "AF"
		});
	}};

	private final JsonObject variant;

	public Anfisa(JsonObject variant) {
		this.variant = variant;
	}

	public JsonObject extractData() {
		JsonObject anfisaJson = new JsonObject();

		for (String key : ASTORAGE_KEY_MAP.keySet()) {
			String[] astorageKeyArray = ASTORAGE_KEY_MAP.get(key);

			String value = extractValueFromAstorage(variant, astorageKeyArray, 0);
			anfisaJson.put(key, value);
		}

		return anfisaJson;
	}
}
