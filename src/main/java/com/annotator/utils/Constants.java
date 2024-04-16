package com.annotator.utils;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

public interface Constants {
	// General:
	int HTTP_SERVER_PORT = 8000;
	int ANNOTATION_EXECUTOR_POOL_SIZE_LIMIT = 4;
	int EXECUTOR_TIME_LIMIT_DAYS = 1;
	String ANNOTATION_EXECUTOR_NAME = "annotation-executor";
	String USER_HOME = System.getProperty("user.home");
	String GS_ANNOTATOR_DIRECTORY_NAME = "/GSAnnotator";
	String ASTORAGE_SERVER_PATH = "http://localhost:8080";
	String HTTP_SERVER_START = "HTTP server started on port: " + HTTP_SERVER_PORT + "!";
	String HTTP_SERVER_STOP = "HTTP server stopped.";

	// Handler URL paths
	String ANNOTATION_HANLDER_PATH = "annotation";

	// Success messages:
	String SUCCESS = "success";

	// Error messages:
	String ERROR = "error";
	String HTTP_SERVER_FAIL = "Server failed to start...";
	String INITIALIZING_DIRECTORY_ERROR = "Couldn't initialize directories...";
	String INTERNAL_ERROR = "Internal error...";

	// Helper functions:
	static void successResponse(HttpServerRequest req, String successMsg) {
		JsonObject successJson = new JsonObject();
		successJson.put(SUCCESS, successMsg);

		if (req.response().ended()) {
			return;
		}

		if (req.response().headWritten()) {
			if (req.response().isChunked()) {
				req.response().write(successJson + "\n");
			} else {
				req.response().end(successJson + "\n");
			}

			return;
		}

		req.response()
			.putHeader("content-type", "application/json")
			.end(successJson + "\n");
	}

	static void errorResponse(HttpServerRequest req, int errorCode, String errorMsg) {
		JsonObject errorJson = new JsonObject();
		errorJson.put(ERROR, errorMsg);

		if (req.response().ended()) {
			return;
		}

		if (req.response().headWritten()) {
			if (req.response().isChunked()) {
				req.response().write(errorJson + "\n");
			} else {
				req.response().end(errorJson + "\n");
			}

			return;
		}

		req.response()
			.setStatusCode(errorCode)
			.putHeader("content-type", "application/json")
			.end(errorJson + "\n");
	}

	static void fileResponse(HttpServerRequest req, String data) {
		req.response()
			.putHeader("content-type", "application/octet-stream")
			.putHeader("content-disposition", "attachment; filename=anfisa.jsonl") // TODO: Filename not working...
			.sendFile(data);
	}
}
