package com.annotator.utils.vcf_file;

import java.util.Arrays;

public class VcfFileHelper {
    public static String[] getVcfGtData(String[] splitVcfLine) {
        if (splitVcfLine.length < 12) {
            return null;
        }

        int indexOfGt = Arrays.stream(splitVcfLine[8].split(":")).toList().indexOf("GT");

        if (indexOfGt == -1) {
            return null;
        }

        String[] fam1Data = splitVcfLine[9].split(":");
        String[] fam2Data = splitVcfLine[10].split(":");
        String[] fam3Data = splitVcfLine[11].split(":");

        return new String[]{fam1Data[indexOfGt], fam2Data[indexOfGt], fam3Data[indexOfGt]};
    }

    /**
     * Maps 0, 1 and 2 to VCF GT data.
     * GT divider might be '/' or '|' and they are not differentiated during parsing.
     * 1/1             -> 2
     * 0/1, 1/0 or 1   -> 1
     * 0/0 or 0        -> 0
     * ./. or .        -> 0 TODO: Double check if correct.
     *
     * @param vcfGtData String array of VCF GT data for all fam members.
     * @return int array of parsed VCF GT data.
     */
    public static Integer[] mapVcfGtData(String[] vcfGtData) {
        if (vcfGtData == null) {
            return null;
        }

        Integer[] parsedVcfGtData = new Integer[vcfGtData.length];
        Arrays.fill(parsedVcfGtData, 0);

        for (int i = 0; i < vcfGtData.length; i++) {
            String currGt = vcfGtData[i];

            if (currGt.length() == 1 && currGt.charAt(0) == '1') {
                parsedVcfGtData[i] = 1;
            } else if ((currGt.charAt(0) == '0' && currGt.charAt(2) == '1')
                    || (currGt.charAt(0) == '1' && currGt.charAt(2) == '0')) {
                parsedVcfGtData[i] = 1;
            } else if (currGt.charAt(0) == '1' && currGt.charAt(2) == '1') {
                parsedVcfGtData[i] = 2;
            }
        }

        return parsedVcfGtData;
    }
}
