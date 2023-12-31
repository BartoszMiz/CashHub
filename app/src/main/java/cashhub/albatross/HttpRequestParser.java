package cashhub.albatross;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class HttpRequestParser {
	public static HttpRequest parseRequest(String requestString, Socket connection) throws MalformedHttpRequestException {
		var requestLines = requestString.split("\r\n");
		var startLine = requestLines[0];

		var requestVerb = parseVerb(startLine);

		var startLineElements = startLine.split(" ");
		if (startLineElements.length != 3) {
			throw new MalformedHttpRequestException("Start line has the wrong format!");
		}

		URI uri;
		try {
			uri = new URI(startLineElements[1]);
		} catch (URISyntaxException e) {
			throw new MalformedHttpRequestException("The URI is malformed!", e);
		}

		var route = uri.getPath();
		var queryParameters = parseQueryParameters(uri.getQuery());
		var headers = parseHeaders(requestLines);
		var cookies = parseCookies(headers.value().get("Cookie"));

		var formData = new HashMap<String, String>();
		if (headers.value().get("Content-Type").equals("application/x-www-form-urlencoded")) {
			formData = parseFormData(requestLines[requestLines.length - 1]);
		}

		return new HttpRequest(requestVerb, route, queryParameters, headers, cookies, formData, connection);
	}

	private static HttpVerb parseVerb(String startLine) {
		for (var verb : HttpVerb.values()) {
			if (startLine.startsWith(verb.toString())) {
				return verb;
			}
		}

		return HttpVerb.UNSUPPORTED;
	}

	private static HttpQueryParameters parseQueryParameters(String paramsString) throws MalformedHttpRequestException {
		var params = new HashMap<String, String>();
		for (var param : paramsString.split("&")) {
			if (!param.contains("=")) {
				throw new MalformedHttpRequestException("Malformed query parameters!");
			}

			var keyValuePair = param.split("=");
			params.put(keyValuePair[0], keyValuePair[1]);
		}
		return new HttpQueryParameters(params);
	}

	private static HashMap<String, String> parseFormData(String formData) throws MalformedHttpRequestException {
		try {
			// Form data and query parameters have the same format name1=value1&name2=value2&...
			return parseQueryParameters(formData).value();
		} catch (MalformedHttpRequestException e) {
			throw new MalformedHttpRequestException("Malformed form data!");
		}
	}

	private static HttpHeaders parseHeaders(String[] requestLines) throws MalformedHttpRequestException {
		var headers = new HashMap<String, String>();
		for (var i = 1; i < requestLines.length; i++) {
			var line = requestLines[i];

			if (line.isEmpty()) {
				break;
			}

			var headerAndValue = line.split(": ");
			if (headerAndValue.length != 2) {
				throw new MalformedHttpRequestException("Headers are malformed!");
			}

			headers.put(headerAndValue[0], headerAndValue[1]);
		}

		return new HttpHeaders(headers);
	}

	private static HashMap<String, String> parseCookies(String cookieString) throws MalformedHttpRequestException {
		var cookies = new HashMap<String, String>();

		for (var cookie : cookieString.split(";")) {
			var cookieNameAndValue = cookie.strip().split("=");
			if (cookieNameAndValue.length != 2) {
				throw new MalformedHttpRequestException("Cookies are malformed!");
			}

			cookies.put(cookieNameAndValue[0], cookieNameAndValue[1]);
		}

		return cookies;
	}
}
