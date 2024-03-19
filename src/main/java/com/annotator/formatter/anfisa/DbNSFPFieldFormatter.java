package com.annotator.formatter.anfisa;

import java.util.HashMap;
import java.util.Map;

public interface DbNSFPFieldFormatter {
    Map<String, Object> ASTORAGE_DBNSFP_KEY_MAP = new HashMap<>() {{
        put("Transcript_id", new String[]{
                "DbNSFP", "variants", "facets", "transcripts", "Ensembl_transcriptid"
        });
    }};
}
