package com.annotator.annotation;

import com.annotator.formatter.anfisa.Anfisa;
import com.annotator.utils.Constants;
import com.annotator.utils.annotation.AnnotatorConstants;
import com.annotator.utils.annotation.AnnotatorHelper;
import com.annotator.utils.astorage.AStorageClient;
import com.annotator.utils.cfg_file.CfgFileHelper;
import com.annotator.utils.fam_file.FamFileHelper;
import com.annotator.utils.files.FilesConstants;
import com.annotator.utils.files.FilesHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Annotator implements Constants, AnnotatorConstants {
	private final RoutingContext context;

	public Annotator(RoutingContext context) {
		this.context = context;
	}

	public String annotationHandler() throws Exception {
		// Local paths of uploaded files
		Map<String, String> filesPaths = FilesHelper.getUploadedFilesPaths(context.fileUploads());

		// CFG file handling:
		JsonObject cfgFileJson = CfgFileHelper.parseCfgFileAsJson(filesPaths.get(FilesConstants.CFG_FILE_EXTENSION));
		String refBuild = CfgFileHelper.getAssemblyVersion(cfgFileJson);

		// FAM file handling:
		JsonArray famFileJson = FamFileHelper.parseFamFileAsJson(filesPaths.get(FilesConstants.FAM_FILE_EXTENSION));
		String phenotypeValue = FamFileHelper.getPhenotypeValue(famFileJson);

		// VCF file handling:
		String vcfFilePath = filesPaths.get(FilesConstants.VCF_FILE_EXTENSION);
		if (vcfFilePath == null) {
			throw new FileNotFoundException(FilesConstants.VCF_FILE_NOT_FOUND);
		}

		String responsesPath = USER_HOME + GS_ANNOTATOR_DIRECTORY_NAME + "/responses";
		Files.createDirectories(Paths.get(responsesPath));
		File responseFile = new File(responsesPath
			+ "/"
			+ FilesHelper.generateUniqueFileName(null, null)
			+ "."
			+ ANNOTATOR_RESULT_FILE_EXTENSION);

		try (
			FileInputStream fileInputStream = new FileInputStream(vcfFilePath);
			InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(reader);
			BufferedWriter writer = new BufferedWriter(new FileWriter(responseFile, true))
		) {
			writer.append(AnnotatorHelper.generateMetadataJson().toString());
			writer.append('\n');

			// Start reading the input file
			String line = bufferedReader.readLine();
			while (line != null && line.startsWith("#")) {
				line = bufferedReader.readLine();
			}

			if (line == null) {
				throw new IOException(INVALID_VCF_ERROR);
			}

			while (line != null) {
				String[] values = line.split("\t");

				if (values.length < 5) {
					line = bufferedReader.readLine();
					continue;
				}

				String chr = values[0].substring(3);
				String pos = values[1];
				String ref = values[3];
				String alt = values[4];

				AStorageClient aStorageClient = new AStorageClient();
				JsonObject universalVariantJson = aStorageClient.queryUniversalVariant(refBuild, chr, pos, ref, alt);

				if (universalVariantJson != null) {
					Anfisa anfisa = new Anfisa(universalVariantJson);
					JsonObject anfisaJson = anfisa.extractData();
					writer.append(anfisaJson.toString());
					writer.append('\n');
				}

				line = bufferedReader.readLine();
			}

			// Output the resulted file path
			return responseFile.getAbsolutePath();
		} catch (Exception e) {
			if (e.getMessage() != null && !e.getMessage().isEmpty()) {
				throw e;
			} else {
				e.printStackTrace();
				throw new Exception(ANNOTATOR_INTERNAL_ERROR);
			}
		}
	}
}
