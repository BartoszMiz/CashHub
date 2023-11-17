package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ConsoleLogger;
import cashhub.logging.LogLevel;

import java.io.IOException;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger(LogLevel.Debug);

		var router = new Router(logger);
		router.addRoute(HttpVerb.GET, "/", request -> {
			try {
				return HttpResponseBuilder.create()
						.fromFile("/index.html")
						.build();
			} catch(IOException e) {
				logger.LogError(String.format("Failed to read file %s: %s", request.url(), e.getMessage()));
				return HttpResponseBuilder
						.create()
						.withDefaultHeaders()
						.withStatusCode(HttpStatusCode.NotFound)
						.build();
			}

		});

		router.addRoute(HttpVerb.GET, "/helloworld", request -> HttpResponseBuilder.create()
			.withStatusCode(HttpStatusCode.OK)
			.withDefaultHeaders()
			.withContent("Hello, world!".getBytes())
			.build());

		var httpServer = new HttpServer(PORT, router, logger);
		if (!httpServer.bind()) {
			return;
		}

		while (true) {
			httpServer.processRequest();
		}
	}
}
