package com.annotator.utils.cfg_file;

import com.annotator.utils.Constants;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class CfgFile implements CfgFileConstants {
	private final JsonObject cfgJson;

	public CfgFile(String cfgFilePath) throws Exception {
		cfgJson = Constants.parseJsonFile(cfgFilePath);
		performFileValidation();
	}

	public String getAssemblyVersion() {
		return cfgJson.getString(ASSEMBLY_VERSION_KEY);
	}

	public Boolean getPerformLiftover() {
		return cfgJson.getBoolean(PERFORM_LIFTOVER_KEY);
	}

	public String getChainFilePath() {
		return cfgJson.getString(CHAIN_FILE_PATH_KEY);
	}

	public String getFastaFilePath() {
		return cfgJson.getString(FASTA_FILE_PATH_KEY);
	}

	private void performFileValidation() throws Exception {
		if (!Arrays.asList(SUPPORTED_ASSEMBLY_VERSIONS).contains(getAssemblyVersion())) {
			throw new Exception(String.format(UNSUPPORTED_ASSEMBLY_ERROR, getAssemblyVersion()));
		}

		if (getPerformLiftover()) {
			String chainFilePath = getChainFilePath();
			String fastaFilePath = getFastaFilePath();

			if (chainFilePath == null || fastaFilePath == null) {
				throw new Exception(CHAIN_FASTA_FILE_PATHS_NOT_PROVIDED);
			}

			File chainFile = new File(chainFilePath);
			File fastaFile = new File(fastaFilePath);
			if (!chainFile.exists() || !fastaFile.exists()) {
				throw new FileNotFoundException(CHAIN_FASTA_FILES_DONT_EXIST);
			}
		}
	}
}
