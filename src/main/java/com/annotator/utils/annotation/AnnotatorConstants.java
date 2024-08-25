package com.annotator.utils.annotation;

public interface AnnotatorConstants {
	// General:
	String ANNOTATOR_RESULT_FILE_EXTENSION = "jsonl";

	// Error messages:
	String INVALID_VCF_ERROR = "Invalid VCF file...";
	String ANNOTATOR_INTERNAL_ERROR = "Annotator internal error...";
	String VCF_FILE_NAME_INVALID = "Given VCF file name should be structured as <case>_<platform>_<project>.vcf";
}
