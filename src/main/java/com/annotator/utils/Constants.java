package com.annotator.utils;

import io.vertx.core.http.HttpServerRequest;

public interface Constants {
	// General:
	int HTTP_SERVER_PORT = 8000;
	String USER_HOME = System.getProperty("user.home");
	String GS_ANNOTATOR_DIRECTORY_NAME = "/GSAnnotator";
	String ASTORAGE_SERVER_PATH = "http://localhost:8080";
	String HTTP_SERVER_START = "HTTP server started on port: " + HTTP_SERVER_PORT + "!";
	String HTTP_SERVER_STOP = "HTTP server stopped.";

	// Handler URL paths
	String ANNOTATION_HANLDER_PATH = "annotation";

	// Error messages:
	String HTTP_SERVER_FAIL = "Server failed to start...";
	String INITIALIZING_DIRECTORY_ERROR = "Couldn't initialize directories...";

	// Helper functions:
	static void errorResponse(HttpServerRequest req, int errorCode, String errorMsg) {
		if (req.response().ended()) {
			return;
		}

		if (req.response().headWritten()) {
			if (req.response().isChunked()) {
				req.response().write(errorMsg + "\n");
			} else {
				req.response().end(errorMsg + "\n");
			}

			return;
		}

		req.response()
				.setStatusCode(errorCode)
				.putHeader("content-type", "text/plain")
				.end(errorMsg + "\n");
	}
}
