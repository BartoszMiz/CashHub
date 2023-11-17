package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ConsoleLogger;
import cashhub.logging.LogLevel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger(LogLevel.Debug);

		var router = new Router(logger);
		router.addRoute(HttpVerb.GET, "/", request ->
			HttpResponseBuilder.create().redirectTo("/index.html")
		);

		router.addRoute(HttpVerb.GET, "/helloworld", request -> HttpResponseBuilder.create()
			.withStatusCode(HttpStatusCode.OK)
			.withDefaultHeaders()
			.withContent("Hello, world!".getBytes())
			.build());

		router.addRoute(HttpVerb.GET, "/hello", request -> {
			try {
				var name =request.parameters().value().get("name");
				if (name == null) {
					name = "person";
				}

				var emojis = new String[] {"\uD83D\uDE80", "\uD83C\uDF86", "\uD83D\uDE0E", "\uD83E\uDD55"};
				var emoji = emojis[new Random().nextInt(emojis.length)];

				var parameters = new HashMap<String, String>();
				parameters.put("name", name);
				parameters.put("emoji", emoji);
				return HttpResponseBuilder.fromTemplate("/hello.html", parameters);
			} catch (IOException e) {
				logger.LogError(String.format("Failed to process template for /hello: %s", e.getMessage()));
				return HttpResponseBuilder
					.create()
					.withDefaultHeaders()
					.withStatusCode(HttpStatusCode.InternalServerError)
					.build();
			}
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
