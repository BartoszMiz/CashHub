package cashhub.albatross;

import java.net.Socket;
import java.util.Map;

public record HttpRequest(
		HttpVerb verb,
		String url,
		Map<String, String> queryParameters,
		Map<String, String> headers,
		Map<String, String> cookies,
		Map<String, String> formData,
		Socket connection
) {}
