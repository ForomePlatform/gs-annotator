package com.annotator.utils.fam_file;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FamFileHelper implements FamFileConstants {
	public static JsonArray parseFamFileAsJson(String famFilePath) throws Exception {
		try (
			FileInputStream fileInputStream = new FileInputStream(famFilePath);
			InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(reader)
		) {
			JsonArray famFileJson = new JsonArray();

			String line = bufferedReader.readLine();
			while (line != null) {
				String[] lineData = line.split("\t");

				if (lineData.length != 6) {
					throw new Exception();
				}

				JsonObject lineDataJson = new JsonObject();
				lineDataJson.put(FAMILY_ID_KEY, lineData[0]);
				lineDataJson.put(WITHIN_FAMILY_ID_KEY, lineData[1]);
				lineDataJson.put(WITHIN_FAMILY_ID_OF_FATHER_KEY, lineData[2]);
				lineDataJson.put(WITHIN_FAMILY_ID_OF_MOTHER_KEY, lineData[3]);
				lineDataJson.put(SEX_CODE_KEY, lineData[4]);
				lineDataJson.put(PHENOTYPE_VALUE_KEY, lineData[5]);

				famFileJson.add(lineDataJson);

				line = bufferedReader.readLine();
			}

			return famFileJson;
		} catch (Exception e) {
			throw new Exception(FAM_PARSING_ERROR);
		}
	}

	public static String getPhenotypeValue(JsonArray famJson) {
		if (famJson.isEmpty()) {
			return MISSING_DATA;
		}

		JsonObject firstLine = famJson.getJsonObject(0);
		String phenotypeValueString = firstLine.getString(PHENOTYPE_VALUE_KEY);

		try {
			int phenotypeValue = Integer.parseInt(phenotypeValueString);

			if (phenotypeValue == CONTROL_KEY) {
				return CONTROL_VALUE;
			} else if (phenotypeValue == CASE_KEY) {
				return CASE_VALUE;
			} else {
				return MISSING_DATA;
			}
		} catch (NumberFormatException e) {
			return MISSING_DATA;
		}
	}
}
