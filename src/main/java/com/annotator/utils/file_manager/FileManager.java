package com.annotator.utils.file_manager;

import io.vertx.ext.web.FileUpload;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManager implements FileManagerConstants {
	private final Map<String, Map<String, String>> uploadedFiles;
	private final String[] splitVcfFileName;

	public FileManager(List<FileUpload> fileUploadList) throws Exception {
		uploadedFiles = processUploadedFiles(fileUploadList);
		performFileValidation();

		splitVcfFileName = getVcfFileName().split("_");
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

	public String getLiftedVcfFilePath() {
		return getVcfFilePath() + LIFTED_VCF_FILE_PATH_SUFFIX;
	}

	public String getLiftedVcfRejectsFilePath() {
		return getVcfFilePath() + LIFTED_VCF_REJECTS_FILE_PATH_SUFFIX;
	}

	public String getCaseValueFromVcf() {
		return splitVcfFileName[0];
	}

	public String getPlatformValueFromVcf() {
		return splitVcfFileName[1];
	}

	public String getProjectValueFromVcf() {
		return splitVcfFileName[2];
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

	private static void storeFileInfo(FileUpload fileUpload, Map<String, Map<String, String>> uploadedFiles) throws Exception {
		String fileExtension = getFileExtension(fileUpload.fileName());

		if (!(fileExtension.equals(CFG_FILE_EXTENSION)
				|| fileExtension.equals(FAM_FILE_EXTENSION)
				|| fileExtension.equals(VCF_FILE_EXTENSION))) {
			throw new Exception(String.format(INVALID_FILE_EXTENSION, fileUpload.fileName()));
		}

		if (!uploadedFiles.containsKey(fileExtension)) {
			System.out.printf("Received file: %s. Server path: %s%n", fileUpload.fileName(), fileUpload.uploadedFileName());

			Map<String, String> fileInfo = new HashMap<>();
			fileInfo.put(FILE_PATH_KEY, fileUpload.uploadedFileName());
			fileInfo.put(FILE_NAME_KEY, removeFileExtension(fileUpload.fileName()));

			uploadedFiles.put(fileExtension, fileInfo);
		} else {
			boolean uploadCancelled = fileUpload.cancel();
			if (!uploadCancelled) fileUpload.delete();
		}
	}

	private void performFileValidation() throws Exception {
		if (!uploadedFiles.containsKey(CFG_FILE_EXTENSION)) {
			throw new FileNotFoundException(CFG_FILE_NOT_FOUND);
		}

		if (!uploadedFiles.containsKey(FAM_FILE_EXTENSION)) {
			throw new FileNotFoundException(FAM_FILE_NOT_FOUND);
		}

		if (!uploadedFiles.containsKey(VCF_FILE_EXTENSION)) {
			throw new FileNotFoundException(VCF_FILE_NOT_FOUND);
		}

		String[] splitVcfFileName = getVcfFileName().split("_");

		if (splitVcfFileName.length != 3) {
			throw new Exception(VCF_FILE_NAME_INVALID);
		}
	}

	public static String getFileExtension(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return "";
		}

		int lastDotIndex = filePath.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
			return "";
		}

		return filePath.substring(lastDotIndex + 1);
	}

	public static String removeFileExtension(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return filePath;
		}

		int lastDotIndex = filePath.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
			return filePath;
		}

		return filePath.substring(0, lastDotIndex);
	}
}
