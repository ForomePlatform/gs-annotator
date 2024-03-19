package com.annotator.formatter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface Formatter {
	JsonObject extractData();

	static String extractValueFromAStorage(JsonObject jsonObject, String[] astorageKeyArray, int currentIndex) {
		if (currentIndex < astorageKeyArray.length) {
			String astorageKey = astorageKeyArray[currentIndex];

			if (jsonObject.containsKey(astorageKey)) {
				Object value = jsonObject.getValue(astorageKey);
				if (value instanceof JsonObject) {
					return extractValueFromAStorage((JsonObject) value, astorageKeyArray, currentIndex + 1);
				} else if (value instanceof JsonArray && !((JsonArray) value).isEmpty()) {
					Object arrayValue = ((JsonArray) value).getValue(0);

					if (arrayValue instanceof String) {
						return (String) arrayValue;
					} else if (arrayValue instanceof JsonObject) {
						return extractValueFromAStorage((JsonObject) arrayValue, astorageKeyArray, currentIndex + 1);
					}
				} else if (value instanceof String) {
					return (String) value;
				}
			}
		}

		System.out.print("Unsupported JSON structure for key \"");
		System.out.print(astorageKeyArray[currentIndex]);
		System.out.print("\" inside the following JSON object: ");
		System.out.println(jsonObject);
		return "";
	}
}
