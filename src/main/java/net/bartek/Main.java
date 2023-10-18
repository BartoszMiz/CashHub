package net.bartek;

import net.bartek.albatross.*;
import net.bartek.logging.ConsoleLogger;
import net.bartek.logging.ILogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		ILogger logger = new ConsoleLogger();
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(PORT);
			logger.LogInformation(String.format("Starting server on port %s", PORT));
		} catch (IOException e) {
			logger.LogError(String.format("Failed to start server: %s", e.getMessage()));
			return;
		}

		while (true) {
			try {
				var connection = serverSocket.accept();
				logger.LogInformation(String.format("Incoming connection from %s", connection.getRemoteSocketAddress()));

				var requestString = readRequest(connection);
				var httpRequest = parseRequest(requestString);
				logger.LogDebug(httpRequest.toString());

				connection.close();
			}
			catch (IOException e) {
				logger.LogError(String.format("Failed to process request: %s", e.getMessage()));
			}
		}
	}

	private static String readRequest(Socket socket) throws IOException {
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

		in.close();
		return new String(byteArray);
	}

	private static HttpRequest parseRequest(String requestString) {
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

		return new HttpRequest(requestVerb, route, parameters, headers);
	}

	private static HttpParameters parseParameters(String paramsString) {
		var params = new HashMap<String, String>();
		for (var param : paramsString.split("&")) {
			var keyValuePair = param.split("=");
			params.put(keyValuePair[0], keyValuePair[1]);
		}
		return new HttpParameters(params);
	}

	private static HttpHeaders parseHeaders(String[] requestLines) {
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