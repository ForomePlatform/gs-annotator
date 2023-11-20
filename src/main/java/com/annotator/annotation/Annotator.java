package com.annotator.annotation;

import com.annotator.utils.Constants;
import com.annotator.utils.annotation.AnnotatorConstants;
import com.annotator.utils.annotation.AnnotatorHelper;
import com.annotator.utils.astorage.AStorageClient;
import com.annotator.utils.cfg_file.CfgFileConstants;
import com.annotator.utils.cfg_file.CfgFileHelper;
import com.annotator.utils.files.FilesConstants;
import com.annotator.utils.files.FilesHelper;
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

	public String annotationHandler() throws IOException {
		// Local paths of uploaded files
		Map<String, String> filesPaths = FilesHelper.getUploadedFilesPaths(context.fileUploads());

		// CFG file handling:
		JsonObject cfgFileJson = CfgFileHelper.parseCfgFileAsJson(filesPaths.get(FilesConstants.CFG_FILE_EXTENSION));
		if (cfgFileJson == null) {
			throw new IOException(CfgFileConstants.CFG_PARSING_ERROR);
		}
		String refBuild = CfgFileHelper.getAssemblyVersion(cfgFileJson);

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

				String chr = values[0].substring(3);
				String pos = values[1];
				String ref = values[3];
				String alt = values[4];

				AStorageClient aStorageClient = new AStorageClient();
				JsonObject universalVariantJson = aStorageClient.queryUniversalVariant(refBuild, chr, pos, ref, alt);

				if (universalVariantJson != null) {
					writer.append(universalVariantJson.toString());
					writer.append('\n');
				}

				line = bufferedReader.readLine();
			}

			// Output the resulted file path
			return responseFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			if (!e.getMessage().isEmpty()) {
				throw e;
			} else {
				throw new FileNotFoundException("Uploaded VCF file not found...");
			}
		}
	}
}
