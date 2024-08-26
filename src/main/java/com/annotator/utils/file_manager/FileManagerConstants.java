package com.annotator.utils.file_manager;

public interface FileManagerConstants {
	// General:
	String CFG_FILE_EXTENSION = "cfg";
	String FAM_FILE_EXTENSION = "fam";
	String VCF_FILE_EXTENSION = "vcf";
	String FILE_NAME_KEY = "fileName";
	String FILE_PATH_KEY = "filePath";
	String LIFTED_VCF_FILE_PATH_SUFFIX = "_lifted_vcf";
	String LIFTED_VCF_REJECTS_FILE_PATH_SUFFIX = "_lifted_vcf_rejects";

	// Error messages:
	String CFG_FILE_NOT_FOUND = "No CFG file provided...";
	String FAM_FILE_NOT_FOUND = "No FAM file provided...";
	String VCF_FILE_NOT_FOUND = "No VCF file provided...";
	String INVALID_FILE_EXTENSION = "%s has invalid file extension.";
	String VCF_FILE_NAME_INVALID = "Given VCF file name should be structured as <case>_<platform>_<project>.vcf";
}
