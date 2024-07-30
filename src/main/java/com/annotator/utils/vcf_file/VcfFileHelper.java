package com.annotator.utils.vcf_file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VcfFileHelper {
    public static List<String> getVcfGtData(String[] splitVcfLine) {
        if (!(splitVcfLine.length > 8)) {
            return null;
        }

        int indexOfGt = Arrays.asList(splitVcfLine[8].split(":")).indexOf("GT");

        if (indexOfGt == -1) {
            return null;
        }

        return Arrays.asList(splitVcfLine)
                .subList(9, splitVcfLine.length).stream()
                .map((genotypeValues) -> genotypeValues.split(":")[indexOfGt]).toList();
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
    public static List<Integer> mapVcfGtData(List<String> vcfGtData) {
        if (vcfGtData == null) {
            return null;
        }

        List<Integer> parsedVcfGtData = new ArrayList<>(Collections.nCopies(vcfGtData.size(), 0));

        for (int i = 0; i < vcfGtData.size(); i++) {
            String currGt = vcfGtData.get(i);

            if (currGt.length() == 1 && currGt.charAt(0) == '1') {
                parsedVcfGtData.set(i, 1);
            } else if ((currGt.charAt(0) == '0' && currGt.charAt(2) == '1')
                    || (currGt.charAt(0) == '1' && currGt.charAt(2) == '0')) {
                parsedVcfGtData.set(i, 1);
            } else if (currGt.charAt(0) == '1' && currGt.charAt(2) == '1') {
                parsedVcfGtData.set(i, 2);
            }
        }

        return parsedVcfGtData;
    }
}
