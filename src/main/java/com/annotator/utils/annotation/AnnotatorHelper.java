package com.annotator.utils.annotation;

import com.annotator.utils.Constants;
import io.vertx.core.json.JsonObject;

public class AnnotatorHelper implements Constants {
	public static JsonObject generateMetadataJson() {
		JsonObject metadataJson = new JsonObject();

		metadataJson.put("record_type", "metadata");
		metadataJson.put("data_schema", "CASE");

		return metadataJson;
	}
}
