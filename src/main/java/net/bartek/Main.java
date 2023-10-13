package net.bartek;

import net.bartek.logging.ConsoleLogger;
import net.bartek.logging.ILogger;

import java.io.IOException;
import java.net.ServerSocket;
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

				var in = connection.getInputStream();

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

				var request = new String(byteArray);
				logger.LogDebug('\n' + request);

				HttpVerb requestMethod = null;
				for (var verb : HttpVerb.values()) {
					if (request.startsWith(verb.toString()))
						requestMethod = verb;
				}

				var routeAndParams = request.split("\n")[0].split(" ")[1];
				var route = routeAndParams.split("\\?")[0];

				HashMap<String, String> parameters = null;
				if (routeAndParams.contains("?")) {
					var paramString = routeAndParams.split("\\?")[1];
					parameters = parseParameters(paramString);
				}

				// TODO: Add parsing for HTTP headers such as Host, User-Agent, etc.

				logger.LogDebug(String.format("Method: %s Route: %s Params: %s", requestMethod != null ? requestMethod.toString() : "unknown", route, parameters));

				connection.close();
			}
			catch (IOException e) {
				logger.LogError(String.format("Failed to accept connection: %s", e.getMessage()));
			}

		}
	}

	private static HashMap<String, String> parseParameters(String paramsString) {
		var params = new HashMap<String, String>();
		for (var param : paramsString.split("&")) {
			var keyValuePair = param.split("=");
			params.put(keyValuePair[0], keyValuePair[1]);
		}
		return params;
	}
}