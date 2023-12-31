package cashhub.albatross;

import cashhub.logging.ILogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

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

			HttpRequest httpRequest;
			try {
				httpRequest = HttpRequestParser.parseRequest(requestString, connection);
			} catch (MalformedHttpRequestException e) {
				var response = HttpResponseBuilder.create()
						.withStatusCode(HttpStatusCode.BadRequest)
						.withContent(e.getMessage())
						.build();

				try {
					connection.getOutputStream().write(response.toBytes());
				} catch (IOException ex) {
					logger.LogError(String.format("Failed to send response: %s", ex.getMessage()));
				}

				return;
			}

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


}