package com.annotator.utils.annotation;

import com.annotator.utils.Constants;
import com.annotator.utils.cfg_file.CfgFileHelper;
import com.annotator.utils.fam_file.FamFileHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

import static com.annotator.utils.fam_file.FamFileConstants.CASE_VALUE;

public class AnnotatorHelper implements Constants, AnnotatorConstants {
	public static JsonObject generateMetadataJson(JsonArray famJson, JsonObject cfgJson, String vcfFileName) throws Exception {
		JsonObject metadataJson = new JsonObject();
		String[] splitVcfFileName = vcfFileName.split("_");

		if (splitVcfFileName.length != 3) {
			throw new Exception(VCF_FILE_NAME_INVALID);
		}

		String caseValue = splitVcfFileName[0];
		String platformValue = splitVcfFileName[1];
		String projectValue = splitVcfFileName[2];

		String phenotypeValue = FamFileHelper.getPhenotypeValue(famJson);
		String withinFamilyId = FamFileHelper.getWithinFamilyId(famJson);

		metadataJson.put("record_type", "metadata");
		metadataJson.put("data_schema", ""); // TODO: Determine.

		metadataJson.put("case", caseValue);
		metadataJson.put("platform", platformValue);
		metadataJson.put("project", projectValue);

		metadataJson.put("modes", new JsonArray(List.of(CfgFileHelper.getAssemblyVersion(cfgJson)))); // TODO: For now only one value...

		if (phenotypeValue.equals(CASE_VALUE)) {
			metadataJson.put("proband", withinFamilyId); // TODO: Check if correct ID or what to do when not case...
		}

		metadataJson.put("samples", FamFileHelper.getSamplesForMetadata(famJson));

		metadataJson.put("cohorts", new JsonArray());

		return metadataJson;
	}
}
