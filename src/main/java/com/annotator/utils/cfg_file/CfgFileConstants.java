package com.annotator.utils.cfg_file;

public interface CfgFileConstants {
	// General:
	String ASSEMBLY_VERSION_KEY = "assembly";
	String PERFORM_LIFTOVER_KEY = "performLiftover";
	String CHAIN_FILE_PATH_KEY = "chainFilePath";
	String FASTA_FILE_PATH_KEY = "fastaFilePath";

	// Annotator related:
	String ASSEMBLY_VERSION_GRCH37 = "GRCh37";
	String ASSEMBLY_VERSION_GRCH38 = "GRCh38";
	String[] SUPPORTED_ASSEMBLY_VERSIONS = {
			ASSEMBLY_VERSION_GRCH37,
			ASSEMBLY_VERSION_GRCH38
	};

	// Errors:
	String UNSUPPORTED_ASSEMBLY_ERROR = "Given assembly version: %s is not supported.";
	String CHAIN_FASTA_FILE_PATHS_NOT_PROVIDED = "Chain and/or FASTA file paths not provided but required.";
	String CHAIN_FASTA_FILES_DONT_EXIST = "Provided Chain and/or FASTA files do not exist.";
}
