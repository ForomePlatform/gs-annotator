package com.annotator.utils.annotation;

import com.annotator.utils.Constants;
import com.annotator.utils.cfg_file.CfgFile;
import com.annotator.utils.fam_file.FamFile;
import com.annotator.utils.file_manager.FileManager;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

import static com.annotator.utils.fam_file.FamFileConstants.CASE_VALUE;

public class AnnotatorHelper implements Constants, AnnotatorConstants {
	public static JsonObject generateMetadataJson(FileManager fileManager, CfgFile cfgFile, FamFile famFile) {
		JsonObject metadataJson = new JsonObject();

		String caseValue = fileManager.getCaseValueFromVcf();
		String platformValue = fileManager.getPlatformValueFromVcf();
		String projectValue = fileManager.getProjectValueFromVcf();

		String phenotypeValue = famFile.getPhenotypeValue();
		String withinFamilyId = famFile.getWithinFamilyId();

		metadataJson.put("record_type", "metadata");
		metadataJson.put("data_schema", ""); // TODO: Determine.

		metadataJson.put("case", caseValue);
		metadataJson.put("platform", platformValue);
		metadataJson.put("project", projectValue);

		metadataJson.put("modes", new JsonArray(List.of(cfgFile.getAssemblyVersion()))); // TODO: For now only one value...

		if (phenotypeValue.equals(CASE_VALUE)) {
			metadataJson.put("proband", withinFamilyId);
		}

		metadataJson.put("samples", famFile.getSamplesForMetadata());

		metadataJson.put("cohorts", new JsonArray());

		return metadataJson;
	}
}
