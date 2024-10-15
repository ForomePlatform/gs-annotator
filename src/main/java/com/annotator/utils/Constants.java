package com.annotator.utils;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface Constants {
	// General:
	int DEFAULT_HTTP_SERVER_PORT = 8000;
	String USER_HOME = System.getProperty("user.home");
	String GS_ANNOTATOR_DIRECTORY_NAME = "/GSAnnotator";
	String HTTP_SERVER_START = "HTTP server started on port: %d!";
	String HTTP_SERVER_STOP = "HTTP server stopped.";

	// Config related:
	String DATA_DIRECTORY_PATH_CONFIG_KEY = "dataDirectoryPath";
	String HTTP_SERVER_PORT_CONFIG_KEY = "serverPort";
	String ASTORAGE_SERVER_URL_CONFIG_KEY = "aStorageServerUrl";

	// Executors related:
	String ANNOTATION_EXECUTOR_NAME = "annotation-executor";
	int ANNOTATION_EXECUTOR_POOL_SIZE_LIMIT = 4;
	int EXECUTOR_TIME_LIMIT_DAYS = 1;

	// URL paths:
	String ANNOTATION_HANLDER_PATH = "/annotation";

	// Variant related:
	// ToDo: Include all cases
   	String[] NORMALIZED_ALTS = {"A", "T", "G", "C", "U"};

	// Success messages:
	String SUCCESS = "success";

	// Error messages:
	String ERROR = "error";
	String HTTP_SERVER_FAIL = "Server failed to start...";
	String INITIALIZING_DIRECTORY_ERROR = "Couldn't initialize directories...";
	String INTERNAL_ERROR = "Internal error...";
	String JSON_FILE_DOESNT_EXIST_ERROR = "Given file: %s, doesn't exist.";
	String JSON_FILE_NOT_READABLE_ERROR = "Couldn't read the given file...";
	String JSON_FILE_DECODE_ERROR = "Given file isn't a valid JSON...";

	// Helper functions:
	static void response(HttpServerRequest req, JsonObject response, int statusCode) {
		if (req.response().ended()) {
			return;
		}

		if (req.response().headWritten()) {
			if (req.response().isChunked()) {
				req.response().write(response + "\n");
			} else {
				req.response().end(response + "\n");
			}

			return;
		}

		req.response()
				.setStatusCode(statusCode)
				.putHeader("content-type", "application/json")
				.end(response + "\n");
	}

	static void successResponse(HttpServerRequest req, String successMsg) {
		JsonObject successJson = new JsonObject();
		successJson.put(SUCCESS, successMsg);

		response(req, successJson, 200);
	}

	static void errorResponse(HttpServerRequest req, int errorCode, String errorMsg) {
		JsonObject errorJson = new JsonObject();
		errorJson.put(ERROR, errorMsg);

		response(req, errorJson, errorCode);
	}

	static void fileResponse(HttpServerRequest req, String data) {
		req.response()
			.putHeader("content-type", "application/octet-stream")
			.putHeader("content-disposition", "attachment; filename=anfisa.jsonl")
			.sendFile(data);
	}

	static JsonObject parseJsonFile(String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException(String.format(JSON_FILE_DOESNT_EXIST_ERROR, filePath));
		}

		String fileAsString;
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			byte[] fileAsBytes = fileInputStream.readAllBytes();
			fileAsString = new String(fileAsBytes, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new Exception(JSON_FILE_NOT_READABLE_ERROR);
		}

		JsonObject fileAsJson;
		try {
			fileAsJson = new JsonObject(fileAsString);
		} catch (DecodeException e) {
			throw new Exception(JSON_FILE_DECODE_ERROR);
		}

		return fileAsJson;
	}
}
