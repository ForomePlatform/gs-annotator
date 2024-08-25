package com.annotator.utils;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

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

	// Annotator related:
	String ASSEMBLY_VERSION_GRCH37 = "GRCh37";
	String ASSEMBLY_VERSION_GRCH38 = "GRCh38";

	// Success messages:
	String SUCCESS = "success";

	// Error messages:
	String ERROR = "error";
	String HTTP_SERVER_FAIL = "Server failed to start...";
	String INITIALIZING_DIRECTORY_ERROR = "Couldn't initialize directories...";
	String INTERNAL_ERROR = "Internal error...";
	String CONFIG_JSON_DOESNT_EXIST_ERROR = "Given config file doesn't exist.";
	String CONFIG_JSON_NOT_READABLE_ERROR = "Couldn't read the given config file...";
	String CONFIG_JSON_DECODE_ERROR = "Given config file isn't a valid JSON...";

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
}
