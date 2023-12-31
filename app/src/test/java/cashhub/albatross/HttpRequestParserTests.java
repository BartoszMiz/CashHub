package cashhub.albatross;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParserTests {
	@Test
	public void shouldParseStringCorrectly() {
		String requestString =
			"""
			POST /submit-form?source=web&type=form HTTP/1.1\r
			Host: www.example.com\r
			User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r
			Content-Type: application/x-www-form-urlencoded\r
			Cookie: sessionID=abc123; username=johndoe\r
			Content-Length: 27\r
			\r
			name=JohnDoe&age=30&city=NY\r
			""";

		var request = HttpRequestParser.parseRequest(requestString, new Socket());

		assertEquals(HttpVerb.POST, request.verb());
		assertEquals("/submit-form", request.url());

		var expectedQueryParameters = new HashMap<>(Map.of(
				"source", "web",
				"type", "form"
		));
		assertEquals(expectedQueryParameters, request.queryParameters());

		var expectedHeaders = new HashMap<>(Map.of(
			"Host", "www.example.com",
			"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
			"Content-Type", "application/x-www-form-urlencoded",
			"Cookie", "sessionID=abc123; username=johndoe",
			"Content-Length", "27"
		));
		assertEquals(expectedHeaders, request.headers());

		var expectedCookies = new HashMap<>(Map.of(
				"sessionID", "abc123",
				"username", "johndoe"
		));
		assertEquals(expectedCookies, request.cookies());

		var expectedFormData = new HashMap<>(Map.of(
				"name", "JohnDoe",
				"city", "NY",
				"age", "30"
		));
		assertEquals(expectedFormData, request.formData());
	}

	@Test
	public void shouldReturnUnsupportedHttpVerb() {
		String requestString =
				"""
				OPTIONS /submit-form?source=web&type=form HTTP/1.1\r
				Host: www.example.com\r
				User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r
				Content-Type: application/x-www-form-urlencoded\r
				Cookie: sessionID=abc123; username=johndoe\r
				Content-Length: 27\r
				\r
				name=JohnDoe&age=30&city=NY\r
				""";

		var request = HttpRequestParser.parseRequest(requestString, new Socket());

		assertEquals(HttpVerb.UNSUPPORTED, request.verb());
		assertEquals("/submit-form", request.url());

		var expectedQueryParameters = new HashMap<>(Map.of(
				"source", "web",
				"type", "form"
		));
		assertEquals(expectedQueryParameters, request.queryParameters());

		var expectedHeaders = new HashMap<>(Map.of(
				"Host", "www.example.com",
				"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
				"Content-Type", "application/x-www-form-urlencoded",
				"Cookie", "sessionID=abc123; username=johndoe",
				"Content-Length", "27"
		));
		assertEquals(expectedHeaders, request.headers());

		var expectedCookies = new HashMap<>(Map.of(
				"sessionID", "abc123",
				"username", "johndoe"
		));
		assertEquals(expectedCookies, request.cookies());

		var expectedFormData = new HashMap<>(Map.of(
				"name", "JohnDoe",
				"city", "NY",
				"age", "30"
		));
		assertEquals(expectedFormData, request.formData());
	}

	@Test
	public void shouldThrowExceptionWhenStartLineIsMalformed() {
		String requestString =
			"""
			POST	/submit-form?source=web&type=form-HTTP/1.1\r
			Host: www.example.com\r
			User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r
			Content-Type: application/x-www-form-urlencoded\r
			Cookie: sessionID=abc123; username=johndoe\r
			Content-Length: 27\r
			\r
			name=JohnDoe&age=30&city=NY\r
			""";

		var e = assertThrows(MalformedHttpRequestException.class,
				() -> HttpRequestParser.parseRequest(requestString, new Socket()));

		assertEquals("Malformed start line!", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionWhenHeaderIsMalformed() {
		String requestString =
			"""
			POST /submit-form?source=web&type=form HTTP/1.1\r
			Host: www.example.com\r
			User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r
			Content-Type: application/x-www-form-urlencoded\r
			Cookie: sessionID=abc123; username=johndoe\r
			Content-Length: 27\r
			\r
			name=JohnDoe&age=30&city=NY\r
			""";

		var e = assertThrows(MalformedHttpRequestException.class,
				() -> HttpRequestParser.parseRequest(requestString, new Socket()));

		assertEquals("Malformed headers!", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionUriIsMalformed() {
		String requestString =
				"""
				POST http:/$#^*4/submit-form?source=web&type=form HTTP/1.1\r
				Host: www.example.com\r
				User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r
				Content-Type: application/x-www-form-urlencoded\r
				Cookie: sessionID=abc123; username=johndoe\r
				Content-Length: 27\r
				\r
				name=JohnDoe&age=30&city=NY\r
				""";

		var e = assertThrows(MalformedHttpRequestException.class,
				() -> HttpRequestParser.parseRequest(requestString, new Socket()));

		assertEquals("Malformed URI!", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionCookieIsMalformed() {
		String requestString =
				"""
				POST /submit-form?source=web&type=form HTTP/1.1\r
				Host: www.example.com\r
				User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r
				Content-Type: application/x-www-form-urlencoded\r
				Cookie: sessionID=abc123&username=johndoe\r
				Content-Length: 27\r
				\r
				name=JohnDoe&age=30&city=NY\r
				""";

		var e = assertThrows(MalformedHttpRequestException.class,
				() -> HttpRequestParser.parseRequest(requestString, new Socket()));

		assertEquals("Malformed cookie!", e.getMessage());
	}

	@Test
	public void shouldThrowExceptionFormDataIsMalformed() {
		String requestString =
				"""
				POST /submit-form?source=web&type=form HTTP/1.1\r
				Host: www.example.com\r
				User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)\r
				Content-Type: application/x-www-form-urlencoded\r
				Cookie: sessionID=abc123; username=johndoe\r
				Content-Length: 27\r
				\r
				name: JohnDoe=&age: 30&=city=NY\r
				""";

		var e = assertThrows(MalformedHttpRequestException.class,
				() -> HttpRequestParser.parseRequest(requestString, new Socket()));

		assertEquals("Malformed form data!", e.getMessage());
	}
}
