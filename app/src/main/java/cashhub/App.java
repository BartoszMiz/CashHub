package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ConsoleLogger;
import cashhub.logging.LogLevel;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger(LogLevel.Debug);

		var router = new Router(logger);
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
