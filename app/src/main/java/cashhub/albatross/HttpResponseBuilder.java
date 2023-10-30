package cashhub.albatross;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class HttpResponseBuilder {
	private HttpStatusCode statusCode;
	private HttpHeaders headers;
	private ArrayList<Byte> content;

	private HttpResponseBuilder() {
		statusCode = HttpStatusCode.OK;
		headers = new HttpHeaders(new HashMap<>());
		content = new ArrayList<>();
	}

	public static HttpResponseBuilder create() {
		return new HttpResponseBuilder();
	}

	public HttpResponseBuilder withStatusCode(HttpStatusCode code) {
		this.statusCode = code;
		return this;
	}

	public HttpResponseBuilder withDefaultHeaders() {
		return this
				.withHeader("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()))
				.withHeader("Server", "Albatross")
				.withHeader("Cache-Control", "no-cache");
	}

	public HttpResponseBuilder withHeader(String name, String value) {
		if (headers.value().containsKey(name)) {
			headers.value().replace(name,value);
		} else {
			headers.value().put(name, value);
		}
		return this;
	}

	public HttpResponseBuilder withContent(Collection<? extends Byte> content) {
		this.content.addAll(content);
		return this;
	}

	public HttpResponseBuilder withContent(byte[] content) {
		for (byte b : content) {
			this.content.add(b);
		}
		return this;
	}

	public HttpResponse build() {
		byte[] bytes;
		if (content.size() == 0) {
			bytes = null;
		} else {
			bytes = new byte[content.size()];
			for (var i = 0; i < bytes.length; i++) {
				bytes[i] = content.get(i);
			}
		}
		return new HttpResponse(statusCode, headers, bytes);
	}
}
