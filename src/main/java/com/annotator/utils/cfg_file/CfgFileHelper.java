package com.annotator.utils.cfg_file;

import io.vertx.core.json.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CfgFileHelper implements CfgFileConstants {
	public static JsonObject parseCfgFileAsJson(String cfgFilePath) {
		try (
			FileInputStream fileInputStream = new FileInputStream(cfgFilePath);
			InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(reader)
		) {
			StringBuilder cfgFile = new StringBuilder();

			String line = bufferedReader.readLine();
			while (line != null) {
				cfgFile.append(line);

				line = bufferedReader.readLine();
			}

			return new JsonObject(cfgFile.toString());
		} catch (IOException e) {
			return null; // TODO: Handle...
		}
	}

	public static String getAssemblyVersion(JsonObject cfgFileJson) {
		return cfgFileJson.getString(ASSEMBLY_FIELD_KEY);
	}
}
