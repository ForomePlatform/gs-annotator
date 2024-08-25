package com.annotator.utils.file_manager;

import io.vertx.ext.web.FileUpload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManager implements FileManagerConstants {
	private final Map<String, Map<String, String>> uploadedFiles;

	public FileManager(List<FileUpload> fileUploadList) throws Exception {
		uploadedFiles = processUploadedFiles(fileUploadList);
	}

	public String getCfgFilePath() {
		return getFilePath(CFG_FILE_EXTENSION);
	}

	public String getCfgFileName() {
		return getFileName(CFG_FILE_EXTENSION);
	}

	public String getFamFilePath() {
		return getFilePath(FAM_FILE_EXTENSION);
	}

	public String getFamFileName() {
		return getFileName(FAM_FILE_EXTENSION);
	}

	public String getVcfFilePath() {
		return getFilePath(VCF_FILE_EXTENSION);
	}

	public String getVcfFileName() {
		return getFileName(VCF_FILE_EXTENSION);
	}

	synchronized public static String generateUniqueFileName(String prefix, String suffix) {
		return (prefix != null ? prefix : "") + System.nanoTime()
				+ (suffix != null ? suffix : "");
	}

	private String getFilePath(String fileExtension) {
		return uploadedFiles.get(fileExtension).get(FILE_PATH_KEY);
	}

	private String getFileName(String fileExtension) {
		return uploadedFiles.get(fileExtension).get(FILE_NAME_KEY);
	}

	private Map<String, Map<String, String>> processUploadedFiles(List<FileUpload> fileUploadList) throws Exception {
		Map<String, Map<String, String>> uploadedFiles = new HashMap<>();

		for (FileUpload fileUpload : fileUploadList) {
			storeFileInfo(fileUpload, uploadedFiles);
		}

		return uploadedFiles;
	}

	private static void storeFileInfo(FileUpload fileUpload, Map<String, Map<String, String>> filePaths) throws Exception {
		String fileExtension = getFileExtension(fileUpload.fileName());

		if (!(fileExtension.equals(CFG_FILE_EXTENSION)
				|| fileExtension.equals(FAM_FILE_EXTENSION)
				|| fileExtension.equals(VCF_FILE_EXTENSION))) {
			throw new Exception(INVALID_FILE_EXTENSION);
		}

		if (!filePaths.containsKey(fileExtension)) {
			System.out.printf("Received file: %s. Server path: %s", fileUpload.fileName(), fileUpload.uploadedFileName());

			Map<String, String> fileInfo = new HashMap<>();
			fileInfo.put(FILE_PATH_KEY, fileUpload.uploadedFileName());
			String fileName = fileUpload.fileName();
			String fileNameWithoutExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
			fileInfo.put(FILE_NAME_KEY, fileNameWithoutExtension);

			filePaths.put(fileExtension, fileInfo);
		} else {
			boolean uploadCancelled = fileUpload.cancel();
			if (!uploadCancelled) fileUpload.delete();
		}
	}

	private static String getFileExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return "";
		}

		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
			return "";
		}

		return fileName.substring(lastDotIndex + 1);
	}
}
