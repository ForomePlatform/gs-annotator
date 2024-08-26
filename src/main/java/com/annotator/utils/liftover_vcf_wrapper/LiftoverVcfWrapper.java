package com.annotator.utils.liftover_vcf_wrapper;

import com.annotator.utils.file_manager.FileManager;
import picard.sam.CreateSequenceDictionary;
import picard.vcf.LiftoverVcf;

import java.io.File;

public class LiftoverVcfWrapper implements LiftoverVcfWrapperConstants {
    private final String chain;
    private final String input;
    private final String output;
    private final String reject;
    private final String refSeq;

    public LiftoverVcfWrapper(String chain, String input, String output, String reject, String refSeq) {
        this.chain = chain;
        this.input = input;
        this.output = output;
        this.reject = reject;
        this.refSeq = refSeq;
    }

    public String liftoverVcf() {
        if (!(new File(FileManager.removeFileExtension(refSeq) + FASTA_DICT_FILE_EXTENSION).exists())) {
            System.out.println(DICT_GENERATION_START_NOTICE);
            CreateSequenceDictionary createSequenceDictionary = new CreateSequenceDictionary();
            String[] createSeqDictArgs = { "--REFERENCE", refSeq };
            createSequenceDictionary.instanceMain(createSeqDictArgs);
            System.out.println(DICT_GENERATION_FINISH_NOTICE);
        } else {
            System.out.println(DICT_PRESENT_NOTICE);
        }

        System.out.println(LIFTOVER_VCF_START_NOTICE);
        LiftoverVcf liftoverVcf = new LiftoverVcf();
        String[] liftoverVcfArgs = {
                "--CHAIN", chain,
                "--INPUT", input,
                "--OUTPUT", output,
                "--REJECT", reject,
                "--REFERENCE_SEQUENCE", refSeq
        };
        liftoverVcf.instanceMain(liftoverVcfArgs);
        System.out.println(LIFTOVER_VCF_FINISH_NOTICE);

        return output;
    }
}
