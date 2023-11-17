package com.annotator.main;

import com.annotator.utils.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MainVerticle extends AbstractVerticle implements Constants {

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
		System.out.println(HTTP_SERVER_STOP);

		stopPromise.complete();
	}

	private void setAnnotationHandler(Router router) {
		// Enable file uploads
		router.route().handler(BodyHandler.create().setUploadsDirectory(
			USER_HOME + GS_ANNOTATOR_DIRECTORY_NAME + "/uploads"
		));

		router.post('/' + ANNOTATION_HANLDER_PATH).handler((RoutingContext context) -> {
			HttpServerRequest request = context.request();

			WorkerExecutor executor = vertx.createSharedWorkerExecutor("annotation", 8, 1, TimeUnit.DAYS);

			Callable<String> callable = () -> {
				String famFilePath = null;
				String cfgFilePath = null;
				String vcfFilePath = null;

				// Store the file in the request
				List<FileUpload> fileUploadList = context.fileUploads();
				for (FileUpload fileUpload : fileUploadList) {
					// Check file extensions and save file path
					famFilePath = checkAndStoreFilePath(".fam", famFilePath, fileUpload);
					cfgFilePath = checkAndStoreFilePath(".cfg", cfgFilePath, fileUpload);
					vcfFilePath = checkAndStoreFilePath(".vcf", vcfFilePath, fileUpload);
				}

				// ToDo: Handle the information in fam and cfg files

				File vcfFile = new File(vcfFilePath);
				File responseFile = new File(USER_HOME + GS_ANNOTATOR_DIRECTORY_NAME + "/responses" + generateUniqueFileName(null, "json"));
				try (
					FileInputStream fileInputStream = new FileInputStream(vcfFile);
					InputStreamReader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
					BufferedReader bufferedReader = new BufferedReader(reader);
					BufferedWriter writer = new BufferedWriter(new FileWriter(responseFile, true))
				) {
					// Start reading the input file
					String line = bufferedReader.readLine();
					while (line != null && line.startsWith("#")) {
						line = bufferedReader.readLine();
					}
					if (line == null) {
						throw new Exception("Invalid VCF file");
					}

					HttpClient client = HttpClient.newHttpClient();

					while (line != null) {
						// Make universal variant query for each variant line
						String[] values = line.split("\t");
						String chr = values[0].substring(3);
						String pos = values[1];
						String ref = values[3];
						String alt = values[4];
						String uriString = ASTORAGE_SERVER_PATH + "/query/universalvariant?refBuild=hg38" + // ToDo: parameterize refBuild
							"&chr=" + chr +
							"&pos=" + pos +
							"&ref=" + ref +
							"&alt=" + alt;

						HttpRequest astorageRequest = HttpRequest.newBuilder()
							.uri(new URI(uriString))
							.GET()
							.build();
						HttpResponse<String> response = client.send(astorageRequest, HttpResponse.BodyHandlers.ofString());

						if (response.statusCode() == HttpURLConnection.HTTP_OK) {
							// ToDo: Convert the AStorage output information to anfisa.json format
							// Append the result to the output response file
							writer.append(response.body());
							writer.append('\n');
						}

						line = bufferedReader.readLine();
					}

					// Output the resulted file path
					return responseFile.getAbsolutePath();
				} catch (Exception e) {
					if (!e.getMessage().isEmpty()) {
						throw e;
					}
				}

				return null;
			};

			executor.executeBlocking(callable, false).onComplete(result -> {
				if (result.succeeded()) {
					// Read the path and send the file as a response
					request.response()
						.putHeader("content-type", "application/octet-stream")
						.putHeader("content-disposition", "attachment; filename=anfisa.jsonl")
						.sendFile(result.result());
				} else {
					Constants.errorResponse(request, HttpURLConnection.HTTP_INTERNAL_ERROR, result.result());
				}

				executor.close();
			});
		});
	}

	private String checkAndStoreFilePath(String extension, String filePath, FileUpload fileUpload) {
		if (fileUpload.fileName().endsWith(extension)) {
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

	private void setStopHandler(Router router) {
		router.get("/stop").handler((RoutingContext context) -> {
			HttpServerRequest req = context.request();

			req.response()
				.putHeader("content-type", "text/plain")
				.end(HTTP_SERVER_STOP + "\n");

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

	synchronized private String generateUniqueFileName(String prefix, String suffix) {
		return (prefix != null ? prefix : "") + System.nanoTime()
			+ (suffix != null ? suffix : "");
	}
}
