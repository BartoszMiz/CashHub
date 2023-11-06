package cashhub.albatross;

import cashhub.logging.ILogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Router {
	public interface RequestDelegate {
		HttpResponse execute(HttpRequest request);
	}

	private final ILogger logger;
	private final HashMap<String, RequestDelegate> routeOverrides;

	public Router(ILogger logger) {
		this.logger = logger;
		this.routeOverrides = new HashMap<>();
	}

	public void addRoute(String route, RequestDelegate delegate) {
		routeOverrides.put(route, delegate);
	}

	public HttpResponse handleRequest(HttpRequest request) {
		var route = request.url();
		HttpResponse response;
		if (routeOverrides.containsKey(route)) {
			response = routeOverrides.get(route).execute(request);
			response.headers().value().putIfAbsent("Content-Type", "application/json");
		} else {
			response = serveStaticContent(request);
		}

		return response;
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
