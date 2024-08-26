package com.annotator.utils.liftover_vcf_wrapper;

public interface LiftoverVcfWrapperConstants {
    // General:
    String FASTA_DICT_FILE_EXTENSION = ".dict";

    // Info messages:
    String DICT_GENERATION_START_NOTICE = "INFO: Appropriate .dict file for the given FASTA will be generated in the same directory...";
    String DICT_GENERATION_FINISH_NOTICE = "INFO: Appropriate .dict file has been successfully generated!";
    String DICT_PRESENT_NOTICE = "INFO: Appropriate .dict file already presents, skipping generation.";
    String LIFTOVER_VCF_START_NOTICE = "INFO: LiftoverVcf started working...";
    String LIFTOVER_VCF_FINISH_NOTICE = "INFO: LiftoverVcf finished working!";
}
