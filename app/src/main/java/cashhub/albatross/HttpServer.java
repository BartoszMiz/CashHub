package cashhub.albatross;

import cashhub.logging.ILogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class HttpServer {
	private final int port;
	private final Router router;
	private final ILogger logger;
	private ServerSocket serverSocket;

	public HttpServer(int port, Router router, ILogger logger) {
		this.port = port;
		this.router = router;
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

		var response = router.handleRequest(httpRequest);

		try (var connection = httpRequest.connection()) {
			connection.getOutputStream().write(response.toBytes());
		} catch (IOException e) {
			logger.LogError(String.format("Failed to send response: %s", e.getMessage()));
		}
	}

	private HttpRequest acceptRequest() {
		Socket connection;
		try {
			connection = serverSocket.accept();
			logger.LogDebug(String.format("Incoming connection from: %s", connection.getRemoteSocketAddress()));
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
			var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			var sb = new StringBuilder();

			String line = "something"; // This string has to have some value before the loop starts
			while (!line.isEmpty() && (line = in.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString().strip();
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