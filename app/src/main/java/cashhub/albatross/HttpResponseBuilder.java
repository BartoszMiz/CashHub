package cashhub.albatross;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HttpResponseBuilder {
	private HttpStatusCode statusCode;
	private final Map<String, String> headers;
	private final Map<String, String> cookies;
	private final List<Byte> content;

	private HttpResponseBuilder() {
		statusCode = HttpStatusCode.OK;
		headers = new HashMap<>();
		cookies = new HashMap<>();
		content = new ArrayList<>();
	}

	private HttpResponseBuilder addDefaultHeaders() {
		return this
				.withHeader("Date", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()))
				.withHeader("Server", "Albatross");
	}

	public static HttpResponseBuilder create() {
		return new HttpResponseBuilder().addDefaultHeaders();
	}

	public HttpResponseBuilder withStatusCode(HttpStatusCode code) {
		this.statusCode = code;
		return this;
	}

	public HttpResponseBuilder withHeader(String name, String value) {
		this.headers.put(name, value);
		return this;
	}

	public HttpResponseBuilder withCookie(String name, String value) {
		this.cookies.put(name, value);
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

	public HttpResponseBuilder withContent(String content) {
		return this.withContent(content.getBytes());
	}

	public HttpResponseBuilder fromFile(String path) throws IOException {
		var file = new File("wwwroot" + path);
		var fileName = file.toPath().getFileName().toString();
		var fileNameSplit = fileName.split("\\.");
		var extension = fileNameSplit[fileNameSplit.length - 1];

		if (!file.exists() && !file.isFile()) {
			return this
					.withStatusCode(HttpStatusCode.NotFound);
		}

		var fileContents = Files.readAllBytes(file.toPath());
		return this
				.withHeader("Content-Type", ExtensionToMimeMapper.getMime(extension))
				.withStatusCode(HttpStatusCode.OK)
				.withContent(fileContents);
	}

	public HttpResponse build() {
		byte[] bytes;
		if (content.isEmpty()) {
			bytes = null;
		} else {
			bytes = new byte[content.size()];
			for (var i = 0; i < bytes.length; i++) {
				bytes[i] = content.get(i);
			}
		}
		return new HttpResponse(statusCode, headers, cookies, bytes);
	}

	public HttpResponseBuilder addRedirect(String url) {
		return withStatusCode(HttpStatusCode.Found)
				.withHeader("Location", url);
	}

	public static HttpResponse redirectTo(String url) {
		return HttpResponseBuilder.create().addRedirect(url).build();
	}

	public static HttpResponse fromTemplate(String templatePath, HashMap<String, String> parameters) throws IOException {
		var file = new File("wwwroot/templates" + templatePath);
		var fileName = file.toPath().getFileName().toString();
		var fileNameSplit = fileName.split("\\.");
		var extension = fileNameSplit[fileNameSplit.length - 1];

		var content = TemplateProcessor.processTemplate(file.toPath(), parameters);
		return HttpResponseBuilder
				.create()
				.withStatusCode(HttpStatusCode.OK)
				.withContent(content)
				.withHeader("Content-Type", ExtensionToMimeMapper.getMime(extension))
				.build();
	}
}
