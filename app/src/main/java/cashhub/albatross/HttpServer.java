package cashhub.albatross;

import cashhub.logging.ILogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
		var connection = acceptConnection();
		if (connection == null) {
			return;
		}

		new Thread(() -> {
			var requestString = readRequest(connection);
			if (requestString == null) {
				return;
			}

			var httpRequest = parseRequest(requestString, connection);
			logger.LogInformation(String.format("Received request for %s", httpRequest.url()));

			var response = router.handleRequest(httpRequest);

			try (var connection1 = httpRequest.connection()) {
				connection1.getOutputStream().write(response.toBytes());
			} catch (IOException e) {
				logger.LogError(String.format("Failed to send response: %s", e.getMessage()));
			}
		}).start();
	}

	private Socket acceptConnection() {
		try {
			var connection = serverSocket.accept();
			logger.LogDebug(String.format("Incoming connection from: %s", connection.getRemoteSocketAddress()));
			return connection;
		} catch (IOException e) {
			logger.LogError(String.format("Failed to accept connection: %s", e.getMessage()));
			return null;
		}
	}


	private String readRequest(Socket socket) {
		try {
			// TODO: Find a better way to read from the socket
			var data = new byte[socket.getInputStream().available()];
			var bytesRead = socket.getInputStream().read(data);
			return new String(Arrays.copyOfRange(data,0,bytesRead)).strip();
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read request: %s", e.getMessage()));
			return null;
		}
	}

	private HttpRequest parseRequest(String requestString, Socket connection) {
		HttpVerb requestVerb = null;
		for (var verb : HttpVerb.values()) {
			if (requestString.startsWith(verb.toString())) {
				requestVerb = verb;
				break;
			}
		}

		var requestLines = requestString.split("\n");
		var routeAndParams = requestLines[0].split(" ")[1];
		var route = routeAndParams.split("\\?")[0];

		HttpParameters parameters = new HttpParameters(new HashMap<>());
		if (requestVerb == HttpVerb.GET) {
			if (routeAndParams.contains("?")) {
				var paramString = routeAndParams.split("\\?")[1];
				parameters = parseParameters(paramString);
			}
		} else {
			parameters = parseParameters(requestLines[requestLines.length - 1]);
		}

		var headers = parseHeaders(requestLines);
		var cookies = parseCookies(headers.value().get("Cookie"));

		return new HttpRequest(requestVerb, route, parameters, headers, cookies, connection);
	}

	private HttpParameters parseParameters(String paramsString) {
		var params = new HashMap<String, String>();
		for (var param : paramsString.split("&")) {
			if (!param.contains("=")) {
				continue;
			}

			var keyValuePair = param.split("=");
			params.put(
					URLDecoder.decode(keyValuePair[0], StandardCharsets.UTF_8),
					URLDecoder.decode(keyValuePair[1], StandardCharsets.UTF_8)
			);
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

	private HashMap<String, String> parseCookies(String cookieString) {
		var cookies = new HashMap<String, String>();

		if (cookieString == null || cookieString.isBlank() || cookieString.isEmpty()) {
			return cookies;
		}

		for (var cookie : cookieString.split(";")) {
			var cookieNameAndValue = cookie.strip().split("=");
			if (cookieNameAndValue.length != 2) {
				continue;
			}

			cookies.put(cookieNameAndValue[0], cookieNameAndValue[1]);
		}

		return cookies;
	}
}