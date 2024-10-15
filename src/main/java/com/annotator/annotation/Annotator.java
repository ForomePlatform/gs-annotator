package com.annotator.annotation;

import com.annotator.formatter.anfisa.Anfisa;
import com.annotator.utils.Constants;
import com.annotator.utils.annotation.AnnotatorConstants;
import com.annotator.utils.annotation.AnnotatorHelper;
import com.annotator.utils.astorage.AStorageClient;
import com.annotator.utils.cfg_file.CfgFile;
import com.annotator.utils.fam_file.FamFile;
import com.annotator.utils.file_manager.FileManager;
import com.annotator.utils.liftover_vcf_wrapper.LiftoverVcfWrapper;
import com.annotator.utils.variant.Variant;
import com.annotator.utils.vcf_file.VcfFileHelper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

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
        CfgFile cfgFile = new CfgFile(cfgFilePath);
        String refBuild = cfgFile.getAssemblyVersion();

        // FAM file handling:
        String famFilePath = fileManager.getFamFilePath();
        FamFile famFile = new FamFile(famFilePath);

        // VCF file handling:
        String vcfFilePath = fileManager.getVcfFilePath();

        // LiftoverVcf step!
        if (cfgFile.getPerformLiftover()) {
            LiftoverVcfWrapper liftoverVcfWrapper = new LiftoverVcfWrapper(
                    cfgFile.getChainFilePath(),
                    vcfFilePath,
                    fileManager.getLiftedVcfFilePath() + ".vcf",
                    fileManager.getLiftedVcfRejectsFilePath() + ".vcf",
                    cfgFile.getFastaFilePath()
            );

            vcfFilePath = liftoverVcfWrapper.liftoverVcf();
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

            List<String> famFileSampleNames = famFile.getSampleNames();
            for (int i = 0; i < famFileSampleNames.size(); i++) {
                sampleNameIndices.put(famFileSampleNames.get(i), i);
            }

            // Add metadata
            writer.append(AnnotatorHelper.generateMetadataJson(fileManager, cfgFile, famFile).toString());
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
                String[] alts = splitVcfLine[4].split(",");
                // Genotype values
                List<String> vcfGtData = VcfFileHelper.getVcfGtData(splitVcfLine);
                List<Integer> mappedGt = VcfFileHelper.mapVcfGtData(vcfGtData);
                List<Integer> mappedSortedGtList =
                        mappedGt == null ? null
                                : IntStream.range(0, mappedGt.size())
                                .mapToObj(i -> new Pair<>(vcfSamples.get(i), mappedGt.get(i)))
                                .sorted(Comparator.comparingInt(gtKeyValue -> sampleNameIndices.get(gtKeyValue.getKey())))
                                .map(Pair::getValue)
                                .toList();

                for (String alt : alts) {
                    JsonObject normalizedVariant = null;
                    String normalizedRef = ref;
                    String normalizedAlt = alt;
                    String normalizedPos = pos;

                    // Normalization step:
                    JsonObject currNormalizedVariant = aStorageClient.normalizeVariant(refBuild, chr, pos, ref, alt);
                    try {
                        if (!(currNormalizedVariant.getString("ref").equals(ref)
                                && currNormalizedVariant.getString("alt").equals(alt)
                                && currNormalizedVariant.getString("pos").equals(pos))) {
                            normalizedVariant = currNormalizedVariant;
                            normalizedRef = normalizedVariant.getString("ref");
                            normalizedAlt = normalizedVariant.getString("alt");
                            normalizedPos = normalizedVariant.getString("pos");
                        }
                    } catch (Exception e) {
                        // TODO: handle normalization error properly
                        throw new Exception(ANNOTATOR_NORMALIZATION_ERROR);
                    }

                    JsonObject universalVariantJson = aStorageClient.queryUniversalVariant(refBuild, chr, normalizedPos, normalizedRef, normalizedAlt);

                    if (universalVariantJson != null) {
                        Variant aStorageVariant = new Variant(chr, normalizedPos, normalizedRef, normalizedAlt, universalVariantJson);
                        aStorageVariant.setMultiallelic(alts.length > 1);
                        aStorageVariant.setAltered(normalizedVariant != null);

                        Anfisa anfisa = new Anfisa(aStorageVariant, mappedSortedGtList);
                        JsonObject anfisaJson = anfisa.extractData();

                        writer.append(anfisaJson.toString());
                        writer.append('\n');
                    }
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
