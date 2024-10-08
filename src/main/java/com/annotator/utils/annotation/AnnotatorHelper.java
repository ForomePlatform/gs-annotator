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
		metadataJson.put("data_schema", "AVARSTAR"); // TODO: Hardcoded, for now.

		metadataJson.put("case", caseValue);
		metadataJson.put("platform", platformValue);
		metadataJson.put("project", projectValue);

		metadataJson.put("modes", new JsonArray(List.of(cfgFile.getAssemblyVersion()))); // TODO: For now only one value...

		if (phenotypeValue.equals(CASE_VALUE)) {
			metadataJson.put("proband", withinFamilyId);
		}

		metadataJson.put("samples", famFile.getSamplesForMetadata());

		// TODO: Hardcoded, for now.
		JsonObject versions = new JsonObject();
		versions.put("pipeline", "???");
		versions.put("reference", "file:///net/bgm/resources/hg19.fasta");
		versions.put("gatk", "3.3-0g37228af");
		versions.put("annotations_date", "2024-09-01");
		versions.put("annotations", "0.6.1");
		versions.put("gatk_select_variants", "3.5-0-g36282e4");
		versions.put("GERP", "hg19.GERP_scores");
		versions.put("annotations_build", "0.6.1.144");
		versions.put("bcftools_annotate_version", "1.3.1-173-gea4ab43+htslib-1.3.2-135-g50db54b");
		metadataJson.put("versions", versions);

		metadataJson.put("cohorts", new JsonArray());

		return metadataJson;
	}
}
