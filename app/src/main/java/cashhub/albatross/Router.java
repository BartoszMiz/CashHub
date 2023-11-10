package cashhub.albatross;

import cashhub.logging.ILogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Router {
	public interface RequestDelegate {
		HttpResponse execute(HttpRequest request);
	}

	private final ILogger logger;
	private final HashMap<String, HashMap<HttpVerb, RequestDelegate>> routeOverrides;

	public Router(ILogger logger) {
		this.logger = logger;
		this.routeOverrides = new HashMap<>();
	}

	public void addRoute(HttpVerb verb, String route, RequestDelegate delegate) {
		var override = new HashMap<HttpVerb, RequestDelegate>();
		override.put(verb, delegate);
		routeOverrides.put(route, override);
	}

	public HttpResponse handleRequest(HttpRequest request) {
		if (routeOverrides.containsKey(request.url())) {
			var override = routeOverrides.get(request.url());
			if (!override.containsKey(request.verb())) {
				logger.LogError(String.format("Unsupported method %s for route %s", request.verb(), request.url()));
				return HttpResponseBuilder.create().withStatusCode(HttpStatusCode.MethodNotAllowed).build();
			}

			var response = override.get(request.verb()).execute(request);
			response.headers().value().putIfAbsent("Content-Type", "application/json");
			return response;
		}

		return serveStaticContent(request);
	}


	private HttpResponse serveStaticContent(HttpRequest httpRequest) {
		var file = new File("wwwroot" + httpRequest.url());
		var fileName = file.toPath().getFileName().toString();
		var fileNameSplit = fileName.split("\\.");
		var extension = fileNameSplit[fileNameSplit.length - 1];

		var response = HttpResponseBuilder.create()
				.withDefaultHeaders()
				.withHeader("Content-Type", String.format("text/%s", extension));

		byte[] fileContents = null;
		try {
			fileContents = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read file: %s", e.getMessage()));
		}

		if (fileContents == null) {
			response.withStatusCode(HttpStatusCode.NotFound);
		} else {
			response.withStatusCode(HttpStatusCode.OK);
			response.withContent(fileContents);
		}

		return response.build();
	}
}
