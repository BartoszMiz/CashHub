package net.bartek.albatross;

import net.bartek.logging.ILogger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpServer {
	private final int port;
	private final ILogger logger;
	private ServerSocket serverSocket;

	public HttpServer(int port, ILogger logger) {
		this.port = port;
		this.logger = logger;
	}

	public boolean bind() {
		try {
			serverSocket = new ServerSocket(port);
			logger.LogInformation(String.format("Starting server on port %s", port));
		} catch (IOException e) {
			logger.LogError(String.format("Failed to start server: %s", e.getMessage()));
			return false;
		}

		return true;
	}

	public void processRequest() {
		var httpRequest = acceptRequest();
		if (httpRequest == null) {
			return;
		}
		logger.LogInformation(String.format("Received request for %s", httpRequest.url()));

		// TODO: Handle routing


		var file = new File("wwwroot" + httpRequest.url());
		var fileName = file.toPath().getFileName().toString();
		var fileNameSplit = fileName.split("\\.");
		var extension = fileNameSplit[fileNameSplit.length - 1];

		byte[] fileContents = null;
		var headers = new HttpHeaders(new HashMap<>());
		headers.value().put("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
		headers.value().put("Server", "Albatross");
		headers.value().put("Cache-Control", "no-cache");
		headers.value().put("Content-Type", String.format("text/%s", extension));
		try {
			fileContents = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read file: %s", e.getMessage()));
		}

		var statusCode = HttpStatusCode.OK;
		if (fileContents == null) {
			statusCode = HttpStatusCode.NotFound;
		}

		var response = new HttpResponse(statusCode, headers, fileContents);
		try(var connection = httpRequest.connection()){
			connection.getOutputStream().write(response.toBytes());
		} catch (IOException e) {
			logger.LogError(String.format("Failed to send response: %s", e.getMessage()));
		}
	}

	private HttpRequest acceptRequest() {
		Socket connection;
		try {
			connection = serverSocket.accept();
			logger.LogInformation(String.format("Incoming connection from: %s", connection.getRemoteSocketAddress()));
		} catch (IOException e) {
			logger.LogError(String.format("Failed to accept connection: %s", e.getMessage()));
			return null;
		}

		var requestString = readRequest(connection);
		if (requestString == null) {
			return null;
		}

		return parseRequest(requestString, connection);
	}

	private String readRequest(Socket socket) {
		try {
			var in = socket.getInputStream();
			var requestBytes = new ArrayList<Byte>();
			var buffer = new byte[256];
			while (in.available() > 0) {
				var readBytes = in.read(buffer);
				for (int i = 0; i < readBytes; i++) {
					requestBytes.add(buffer[i]);
				}
			}

			// bruh Java is high
			var byteArray = new byte[requestBytes.size()];
			for (int i = 0; i < byteArray.length; i++) {
				byteArray[i] = requestBytes.get(i);
			}

			return new String(byteArray);
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read request: %s", e.getMessage()));
			return null;
		}
	}

	private HttpRequest parseRequest(String requestString, Socket connection) {
		HttpVerb requestVerb = null;
		for (var verb : HttpVerb.values()) {
			if (requestString.startsWith(verb.toString()))
				requestVerb = verb;
		}

		var requestLines = requestString.split("\n");
		var routeAndParams = requestLines[0].split(" ")[1];
		var route = routeAndParams.split("\\?")[0];

		HttpParameters parameters = null;
		if (routeAndParams.contains("?")) {
			var paramString = routeAndParams.split("\\?")[1];
			parameters = parseParameters(paramString);
		}

		var headers = parseHeaders(requestLines);

		return new HttpRequest(requestVerb, route, parameters, headers, connection);
	}

	private HttpParameters parseParameters(String paramsString) {
		var params = new HashMap<String, String>();
		for (var param : paramsString.split("&")) {
			var keyValuePair = param.split("=");
			params.put(keyValuePair[0], keyValuePair[1]);
		}
		return new HttpParameters(params);
	}

	private HttpHeaders parseHeaders(String[] requestLines) {
		var headers = new HashMap<String, String>();
		for (var i = 1; i < requestLines.length; i++) {
			var line = requestLines[i].strip();
			var headerAndValue = line.split(": ");

			if (headerAndValue.length != 2) {
				continue;
			}

			headers.put(headerAndValue[0], headerAndValue[1]);
		}

		return new HttpHeaders(headers);
	}
}
