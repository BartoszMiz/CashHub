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
			var headers = new HttpHeaders(new HashMap<>());
			headers.value().put("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
			headers.value().put("Server", "Albatross");
			headers.value().put("Cache-Control", "no-cache");
			headers.value().put("Content-Type", "application/json");

			var responseBytes = "\"Hello, world!\"".getBytes();
			var httpResponse = new HttpResponse(HttpStatusCode.OK, headers, responseBytes);
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
