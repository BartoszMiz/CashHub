package cashhub.albatross;

import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HttpRequestParser {
	public static HttpRequest parseRequest(String requestString, Socket connection) {
		HttpVerb requestVerb = null;
		for (var verb : HttpVerb.values()) {
			if (requestString.startsWith(verb.toString())) {
				requestVerb = verb;
				break;
			}
		}

		var requestLines = requestString.split("\n");
		var routeAndParams = requestLines[0].split(" ")[1];
		var route = routeAndParams.split("\\?")[0];

		HttpParameters parameters = new HttpParameters(new HashMap<>());
		if (requestVerb == HttpVerb.GET) {
			if (routeAndParams.contains("?")) {
				var paramString = routeAndParams.split("\\?")[1];
				parameters = parseParameters(paramString);
			}
		} else {
			parameters = parseParameters(requestLines[requestLines.length - 1]);
		}

		var headers = parseHeaders(requestLines);
		var cookies = parseCookies(headers.value().get("Cookie"));

		return new HttpRequest(requestVerb, route, parameters, headers, cookies, connection);
	}

	private static HttpParameters parseParameters(String paramsString) {
		var params = new HashMap<String, String>();
		for (var param : paramsString.split("&")) {
			if (!param.contains("=")) {
				continue;
			}

			var keyValuePair = param.split("=");
			params.put(
					URLDecoder.decode(keyValuePair[0], StandardCharsets.UTF_8),
					URLDecoder.decode(keyValuePair[1], StandardCharsets.UTF_8)
			);
		}
		return new HttpParameters(params);
	}

	private static HttpHeaders parseHeaders(String[] requestLines) {
		var headers = new HashMap<String, String>();
		for (var i = 1; i < requestLines.length; i++) {
			var line = requestLines[i].strip();
			var headerAndValue = line.split(": ");

			if (headerAndValue.length != 2) {
				continue;
			}

			headers.put(headerAndValue[0], headerAndValue[1]);
		}

		return new HttpHeaders(headers);
	}

	private static HashMap<String, String> parseCookies(String cookieString) {
		var cookies = new HashMap<String, String>();

		if (cookieString == null || cookieString.isBlank() || cookieString.isEmpty()) {
			return cookies;
		}

		for (var cookie : cookieString.split(";")) {
			var cookieNameAndValue = cookie.strip().split("=");
			if (cookieNameAndValue.length != 2) {
				continue;
			}

			cookies.put(cookieNameAndValue[0], cookieNameAndValue[1]);
		}

		return cookies;
	}
}
