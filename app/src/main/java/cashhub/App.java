package cashhub;

import cashhub.albatross.HttpResponseBuilder;
import cashhub.albatross.HttpServer;
import cashhub.albatross.HttpStatusCode;
import cashhub.albatross.Router;
import cashhub.logging.ConsoleLogger;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger();

		var router = new Router(logger);
		router.addRoute("/helloworld", request -> HttpResponseBuilder.create()
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
