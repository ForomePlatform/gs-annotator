package com.annotator.annotation;

import com.annotator.formatter.anfisa.Anfisa;
import com.annotator.utils.Constants;
import com.annotator.utils.annotation.AnnotatorConstants;
import com.annotator.utils.annotation.AnnotatorHelper;
import com.annotator.utils.astorage.AStorageClient;
import com.annotator.utils.cfg_file.CfgFileHelper;
import com.annotator.utils.fam_file.FamFileHelper;
import com.annotator.utils.file_manager.FileManager;
import com.annotator.utils.file_manager.FileManagerConstants;
import com.annotator.utils.vcf_file.VcfFileHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

import static com.annotator.utils.fam_file.FamFileConstants.WITHIN_FAMILY_ID_KEY;

public class Annotator implements Constants, AnnotatorConstants {
	private final RoutingContext context;
	private final String dataDirectoryPath;
	private final String aStorageServerUrl;

	public Annotator(RoutingContext context, String dataDirectoryPath, String aStorageServerUrl) {
		this.context = context;
		this.dataDirectoryPath = dataDirectoryPath;
		this.aStorageServerUrl = aStorageServerUrl;
	}

	public String annotationHandler() throws Exception {
		// Names and local paths of uploaded files
		FileManager fileManager = new FileManager(context.fileUploads());

		// CFG file handling:
		String cfgFilePath = fileManager.getCfgFilePath();
		if (cfgFilePath == null) {
			throw new FileNotFoundException(FileManagerConstants.CFG_FILE_NOT_FOUND);
		}
		JsonObject cfgJson = CfgFileHelper.parseCfgFileAsJson(cfgFilePath);
		String refBuild = CfgFileHelper.getAssemblyVersion(cfgJson);

		// FAM file handling:
		String famFilePath = fileManager.getFamFilePath();
		if (famFilePath == null) {
			throw new FileNotFoundException(FileManagerConstants.FAM_FILE_NOT_FOUND);
		}
		JsonArray famJson = FamFileHelper.parseFamFileAsJson(famFilePath);

		// VCF file handling:
		String vcfFilePath = fileManager.getVcfFilePath();
		if (vcfFilePath == null) {
			throw new FileNotFoundException(FileManagerConstants.VCF_FILE_NOT_FOUND);
		}

		AStorageClient aStorageClient = new AStorageClient(aStorageServerUrl);

		String responsesPath = dataDirectoryPath + "/responses";
		Files.createDirectories(Paths.get(responsesPath));
		File responseFile = new File(responsesPath
			+ "/"
			+ FileManager.generateUniqueFileName(null, null)
			+ "."
			+ ANNOTATOR_RESULT_FILE_EXTENSION);

		try (
			FileInputStream fileInputStream = new FileInputStream(vcfFilePath);
			InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(reader);
			BufferedWriter writer = new BufferedWriter(new FileWriter(responseFile, true))
		) {
			// Start reading the input file
			String line = bufferedReader.readLine();
			while (line != null && line.startsWith("##")) {
				line = bufferedReader.readLine();
			}

			if (line == null || !line.startsWith("#")) {
				throw new IOException(INVALID_VCF_ERROR);
			}

			List<String> vcfSamples = new ArrayList<>();
			Map<String, Integer> sampleNameIndices = new HashMap<>();
			String[] splitVcfLine = line.split("\t");
			if (splitVcfLine.length > 8) {
				vcfSamples.addAll(Arrays.asList(splitVcfLine).subList(9, splitVcfLine.length));
			}

			for (int i = 0; i < famJson.size(); i++) {
				String sampleName = famJson.getJsonObject(i).getString(WITHIN_FAMILY_ID_KEY);
				sampleNameIndices.put(sampleName, i);
			}

			// Add metadata
			writer.append(AnnotatorHelper.generateMetadataJson(famJson, cfgJson, fileManager.getVcfFileName()).toString());
			writer.append('\n');

			line = bufferedReader.readLine();

			while (line != null) {
				splitVcfLine = line.split("\t");

				if (splitVcfLine.length < 5) {
					line = bufferedReader.readLine();
					continue;
				}

				String chr = splitVcfLine[0].substring(3);
				String pos = splitVcfLine[1];
				String ref = splitVcfLine[3];
				String alt = splitVcfLine[4];
				List<String> vcfGtData = VcfFileHelper.getVcfGtData(splitVcfLine);
				List<Integer> mappedGt = VcfFileHelper.mapVcfGtData(vcfGtData);
				List<Integer> mappedSortedGtList =
						mappedGt == null ? null
						: IntStream.range(0, mappedGt.size())
								.mapToObj(i -> new Pair<>(vcfSamples.get(i), mappedGt.get(i)))
								.sorted(Comparator.comparingInt(gtKeyValue -> sampleNameIndices.get(gtKeyValue.getKey())))
								.map(Pair::getValue)
								.toList();

				JsonObject universalVariantJson = aStorageClient.queryUniversalVariant(refBuild, chr, pos, ref, alt);

				if (universalVariantJson != null) {
					Anfisa anfisa = new Anfisa(universalVariantJson, famJson, mappedSortedGtList);
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
