package com.annotator.utils.files;

import io.vertx.ext.web.FileUpload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilesHelper implements FilesConstants {
	public static Map<String, String> getUploadedFilesPaths(List<FileUpload> fileUploadList) {
		String cfgFilePath = null;
		String famFilePath = null;
		String vcfFilePath = null;

		for (FileUpload fileUpload : fileUploadList) {
			cfgFilePath = checkAndStoreFilePath(CFG_FILE_EXTENSION, cfgFilePath, fileUpload);
			famFilePath = checkAndStoreFilePath(FAM_FILE_EXTENSION, famFilePath, fileUpload);
			vcfFilePath = checkAndStoreFilePath(VCF_FILE_EXTENSION, vcfFilePath, fileUpload);
		}

		Map<String, String> filePaths = new HashMap<>();
		filePaths.put(CFG_FILE_EXTENSION, cfgFilePath);
		filePaths.put(FAM_FILE_EXTENSION, famFilePath);
		filePaths.put(VCF_FILE_EXTENSION, vcfFilePath);

		return filePaths;
	}

	private static String checkAndStoreFilePath(String extension, String filePath, FileUpload fileUpload) {
		if (fileUpload.fileName().endsWith("." + extension)) {
			if (filePath == null) {
				System.out.println("Received file: " + fileUpload.fileName() + " uploaded to " + fileUpload.uploadedFileName());

				return fileUpload.uploadedFileName();
			} else {
				boolean uploadCancelled = fileUpload.cancel();
				if (!uploadCancelled) fileUpload.delete();

				return filePath;
			}
		}

		return filePath;
	}

	synchronized public static String generateUniqueFileName(String prefix, String suffix) {
		return (prefix != null ? prefix : "") + System.nanoTime()
			+ (suffix != null ? suffix : "");
	}
}
