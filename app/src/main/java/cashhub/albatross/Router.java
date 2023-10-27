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
		if (routeOverrides.containsKey(route)) {
			return routeOverrides.get(route).execute(request);
		}

		return serveStaticContent(request);
	}


	private HttpResponse serveStaticContent(HttpRequest httpRequest) {
		var file = new File("wwwroot" + httpRequest.url());
		var fileName = file.toPath().getFileName().toString();
		var fileNameSplit = fileName.split("\\.");
		var extension = fileNameSplit[fileNameSplit.length - 1];

		byte[] fileContents = null;
		var headers = new HttpHeaders(new HashMap<>());
		headers.value().put("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
		headers.value().put("Server", "Albatross");
		headers.value().put("Cache-Control", "no-cache");
		headers.value().put("Content-Type", String.format("text/%s", extension));
		try {
			fileContents = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read file: %s", e.getMessage()));
		}

		var statusCode = HttpStatusCode.OK;
		if (fileContents == null) {
			statusCode = HttpStatusCode.NotFound;
		}

		return new HttpResponse(statusCode, headers, fileContents);
	}
}
