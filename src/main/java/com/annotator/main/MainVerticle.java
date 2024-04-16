package com.annotator.main;

import com.annotator.annotation.Annotator;
import com.annotator.utils.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MainVerticle extends AbstractVerticle implements Constants {
	WorkerExecutor annotationExecutor;

	@Override
	public void start(Promise<Void> startPromise) {
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		String dataDirectoryPath;
		try {
			dataDirectoryPath = getDataDirectoryPath();
			System.out.println("Data directory path: " + dataDirectoryPath);
		} catch (Exception e) {
			startPromise.fail(e.getMessage());

			return;
		}

		annotationExecutor = vertx.createSharedWorkerExecutor(
			ANNOTATION_EXECUTOR_NAME,
			ANNOTATION_EXECUTOR_POOL_SIZE_LIMIT,
			EXECUTOR_TIME_LIMIT_DAYS,
			TimeUnit.DAYS
		);

		setAnnotationHandler(router);
		setStopHandler(router);

		server.requestHandler(router).listen(HTTP_SERVER_PORT, result -> {
			if (result.succeeded()) {
				if (!initializeDirectories(dataDirectoryPath)) {
					startPromise.fail(new IOException(INITIALIZING_DIRECTORY_ERROR));

					return;
				}

				System.out.println(HTTP_SERVER_START);
				startPromise.complete();
			} else {
				System.err.println(HTTP_SERVER_FAIL);
				result.cause().printStackTrace();
				startPromise.fail(result.cause());
			}
		});
	}

	@Override
	public void stop(Promise<Void> stopPromise) {
		annotationExecutor.close();

		System.out.println(HTTP_SERVER_STOP);

		stopPromise.complete();
	}

	private void setAnnotationHandler(Router router) {
		// Enable file uploads
		router.route().handler(BodyHandler.create().setUploadsDirectory(
			USER_HOME + GS_ANNOTATOR_DIRECTORY_NAME + "/uploads"
		));

		router.post('/' + ANNOTATION_HANLDER_PATH).handler((RoutingContext context) -> {
			HttpServerRequest req = context.request();

			Callable<String> callable = () -> {
				Annotator annotator = new Annotator(context);

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

	private String getDataDirectoryPath() {
		return USER_HOME + GS_ANNOTATOR_DIRECTORY_NAME;
	}
}
