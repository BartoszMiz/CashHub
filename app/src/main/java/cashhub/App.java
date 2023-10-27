package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ConsoleLogger;
import cashhub.logging.ILogger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger();

		var router = new Router(logger);
		router.addRoute("/helloworld", request -> {
			var responseBytes = "\"Hello, world!\"".getBytes();
			var httpResponse = new HttpResponse(HttpStatusCode.OK, new HttpHeaders(new HashMap<>()), responseBytes);
			return httpResponse;
		});

		var httpServer = new HttpServer(PORT, router, logger);
		if (!httpServer.bind()) {
			return;
		}

		while (true) {
			httpServer.processRequest();
		}
	}
}
