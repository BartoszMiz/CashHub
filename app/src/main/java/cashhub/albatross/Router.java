package cashhub.albatross;

import cashhub.logging.ILogger;

import java.io.IOException;
import java.util.HashMap;

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
			response.headers().value().putIfAbsent("Content-Type", ExtensionToMimeMapper.getMime("json"));
			return response;
		}

		return serveStaticContent(request);
	}

	private HttpResponse serveStaticContent(HttpRequest httpRequest) {
		try {
			return HttpResponseBuilder
					.create()
					.fromFile(httpRequest.url())
					.build();
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read file %s: %s", httpRequest.url(), e.getMessage()));
			return HttpResponseBuilder
					.create()
					.withStatusCode(HttpStatusCode.InternalServerError)
					.build();
		}
	}
}
