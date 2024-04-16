package com.annotator.formatter.anfisa;

import com.annotator.formatter.Formatter;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Anfisa {
	public static final String[] ANFISA_FIELD_GROUPS = {
		"GnomAD"
	};

	private final Map<String, Object> preprocessedData;
	private final JsonObject anfisaJson;
	private final JsonObject variant;

	public Anfisa(JsonObject variant) {
		this.variant = variant;
		this.preprocessedData = new HashMap<>();
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
