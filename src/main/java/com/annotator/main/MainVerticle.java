package com.annotator.main;

import com.annotator.annotation.Annotator;
import com.annotator.utils.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MainVerticle extends AbstractVerticle implements Constants {
	private WorkerExecutor annotationExecutor;
	private String aStorageServerUrl;

	@Override
	public void start(Promise<Void> startPromise) {
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		JsonObject configJson;
		String dataDirectoryPath;
		Integer serverPort = DEFAULT_HTTP_SERVER_PORT;
		try {
			configJson = getConfigJson();

			dataDirectoryPath = getDataDirectoryPath(configJson.getString(DATA_DIRECTORY_PATH_CONFIG_KEY));
			System.out.println("Data directory path: " + dataDirectoryPath);

			createLogFile(dataDirectoryPath);

			if (configJson.containsKey(HTTP_SERVER_PORT_CONFIG_KEY)) {
				serverPort = configJson.getInteger(HTTP_SERVER_PORT_CONFIG_KEY);
			}

			if (configJson.containsKey(ASTORAGE_SERVER_URL_CONFIG_KEY)) {
				aStorageServerUrl = configJson.getString(ASTORAGE_SERVER_URL_CONFIG_KEY);
			} else {
				throw new Exception("AStorage server URL not provided...");
			}
		} catch (Exception e) {
			startPromise.fail(e.getMessage());

			return;
		}

		WorkerExecutor initExecutor = vertx.createSharedWorkerExecutor("init-executor", 1, 1, TimeUnit.DAYS);
		Callable<Boolean> callableInit = init(router, dataDirectoryPath);

		Integer finalServerPort = serverPort;
		initExecutor.executeBlocking(callableInit).onComplete(handler -> {
			initExecutor.close();

			server.requestHandler(router).listen(finalServerPort, result -> {
				if (result.succeeded()) {
					if (!initializeDirectories(dataDirectoryPath)) {
						startPromise.fail(new IOException(INITIALIZING_DIRECTORY_ERROR));

						return;
					}

					System.out.printf((HTTP_SERVER_START) + "%n", finalServerPort);
					startPromise.complete();
				} else {
					System.err.println(HTTP_SERVER_FAIL);
					result.cause().printStackTrace();
					startPromise.fail(result.cause());
				}
			});
		});
	}

	@Override
	public void stop(Promise<Void> stopPromise) {
		annotationExecutor.close();

		System.out.println(HTTP_SERVER_STOP);

		stopPromise.complete();
	}

	private Callable<Boolean> init(Router router, String dataDirectoryPath) {
		return () -> {
			annotationExecutor = vertx.createSharedWorkerExecutor(
				ANNOTATION_EXECUTOR_NAME,
				ANNOTATION_EXECUTOR_POOL_SIZE_LIMIT,
				EXECUTOR_TIME_LIMIT_DAYS,
				TimeUnit.DAYS
			);

			setAnnotationHandler(router, dataDirectoryPath);
			setStopHandler(router);
			setSwaggerHandler(router);

			return true;
		};
	}

	private void setAnnotationHandler(Router router, String dataDirectoryPath) {
		// Enable file uploads
		router.route().handler(BodyHandler.create().setUploadsDirectory(dataDirectoryPath + "/uploads"));

		router.post(ANNOTATION_HANLDER_PATH + "/anfisa" ).handler((RoutingContext context) -> {
			HttpServerRequest req = context.request();

			Callable<String> callable = () -> {
				System.out.println(ANNOTATION_EXECUTOR_NAME + " started working...");

				Annotator annotator = new Annotator(context, dataDirectoryPath, aStorageServerUrl);

				return annotator.annotationHandler();
			};

			annotationExecutor.executeBlocking(callable, false).onComplete(result -> {
				if (result.succeeded()) {
					Constants.fileResponse(req, result.result());
				} else {
					String errorMessage = INTERNAL_ERROR;

					if (result.cause().getMessage() != null) {
						errorMessage = result.cause().getMessage();
					}

					Constants.errorResponse(req, HttpURLConnection.HTTP_INTERNAL_ERROR, errorMessage);
				}

				System.out.println(ANNOTATION_EXECUTOR_NAME + " finished working!");
			});
		});
	}

	private void setStopHandler(Router router) {
		router.get("/stop").handler((RoutingContext context) -> {
			HttpServerRequest req = context.request();

			Constants.successResponse(req, HTTP_SERVER_STOP);

			vertx.close();
		});
	}

	private void setSwaggerHandler(Router router) {
		router.route("/*").handler(StaticHandler.create());
	}

	private boolean initializeDirectories(String dataDirectoryPath) {
		try {
			File dataDir = new File(dataDirectoryPath);
			if (!dataDir.exists() && !dataDir.mkdirs()) {
				return false;
			}
		} catch (SecurityException e) {
			return false;
		}

		return true;
	}

	private String getDataDirectoryPath(String dataDirectoryPathFromConfig) {
		if (dataDirectoryPathFromConfig != null) {
			return dataDirectoryPathFromConfig;
		}

		return USER_HOME + GS_ANNOTATOR_DIRECTORY_NAME;
	}

	private void createLogFile(String dataDirectoryPath) throws Exception {
		File logFile = new File(dataDirectoryPath, "output_" + System.currentTimeMillis() + ".log");
		if (!logFile.exists()) {
			Files.createDirectories(logFile.getParentFile().toPath());
			Files.createFile(logFile.getAbsoluteFile().toPath());
		}

		PrintStream printStream = new PrintStream(new FileOutputStream(logFile));
		System.setOut(printStream);
	}

	private JsonObject getConfigJson() throws Exception {
		List<String> args = Vertx.currentContext().processArgs();

		if (args != null && !args.isEmpty()) {
			String configPath = args.get(0);

			File file = new File(configPath);
			if (!file.exists()) {
				throw new Exception(CONFIG_JSON_DOESNT_EXIST_ERROR);
			}

			String configAsString;
			try (FileInputStream fileInputStream = new FileInputStream(file)) {
				byte[] configAsBytes = fileInputStream.readAllBytes();
				configAsString = new String(configAsBytes, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new Exception(CONFIG_JSON_NOT_READABLE_ERROR);
			}

			JsonObject configAsJson;
			try {
				configAsJson = new JsonObject(configAsString);
			} catch (DecodeException e) {
				throw new Exception(CONFIG_JSON_DECODE_ERROR);
			}

			return configAsJson;
		}

		return new JsonObject();
	}
}
