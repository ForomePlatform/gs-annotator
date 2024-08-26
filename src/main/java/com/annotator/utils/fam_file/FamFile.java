package com.annotator.utils.fam_file;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FamFile implements FamFileConstants {
	private final JsonArray famJson;

	public FamFile(String famFilePath) throws Exception {
		famJson = parseFamFileAsJson(famFilePath);
	}

	public Integer getEntryCount() {
		return famJson.size();
	}

	public List<String> getSampleNames() {
		List<String> sampleNames = new ArrayList<>();

		for (int i = 0; i < getEntryCount(); i++) {
			sampleNames.add(famJson.getJsonObject(i).getString(WITHIN_FAMILY_ID_KEY));
		}

		return sampleNames;
	}

	public String getWithinFamilyId() {
		if (famJson.isEmpty()) {
			return null;
		}

		JsonObject firstLine = famJson.getJsonObject(0);

		return firstLine.getString(WITHIN_FAMILY_ID_KEY);
	}

	public String getPhenotypeValue() {
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

	public JsonObject getSamplesForMetadata() {
		JsonObject result = new JsonObject();

		try {
			for (int i = 0; i < famJson.size(); i++) {
				JsonObject currentLine = famJson.getJsonObject(i);
				JsonObject newObject = new JsonObject();

				newObject.put("mother", currentLine.getString(WITHIN_FAMILY_ID_OF_MOTHER_KEY));
				newObject.put("father", currentLine.getString(WITHIN_FAMILY_ID_OF_FATHER_KEY));
				newObject.put("sex", Integer.parseInt(currentLine.getString(SEX_CODE_KEY)));
				newObject.put("name", currentLine.getString(WITHIN_FAMILY_ID_KEY));
				newObject.put("id", currentLine.getString(WITHIN_FAMILY_ID_KEY));
				newObject.put("family", currentLine.getString(FAMILY_ID_KEY));
				newObject.put("affected", currentLine.getString(PHENOTYPE_VALUE_KEY).equals("2"));

				result.put(currentLine.getString(WITHIN_FAMILY_ID_KEY), newObject);
			}
		} catch (NumberFormatException e) {
			return null; // TODO: Handle...
		}

		return result;
	}

	private static JsonArray parseFamFileAsJson(String famFilePath) throws Exception {
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
}
