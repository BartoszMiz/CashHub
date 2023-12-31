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
POST /submit-form?source=web&type=form HTTP/1.1
Host: www.example.com
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
Content-Type: application/x-www-form-urlencoded
Cookie: sessionID=abc123; username=johndoe
Content-Length: 27

name=JohnDoe&age=30&city=NY					
""";

		var request = HttpRequestParser.parseRequest(requestString, new Socket());

		assertEquals(HttpVerb.POST, request.verb());
		assertEquals("/submit-form", request.url());

		var expectedParameters = new HttpParameters(new HashMap<>(Map.of(
				"name", "JohnDoe",
				"city", "NY",
				"age", "30"
		)));
		assertEquals(expectedParameters, request.parameters());

		var expectedHeaders = new HttpHeaders(new HashMap<>(Map.of(
			"Host", "www.example.com",
			"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
			"Content-Type", "application/x-www-form-urlencoded",
			"Cookie", "sessionID=abc123; username=johndoe",
			"Content-Length", "27"
		)));
		assertEquals(expectedHeaders, request.headers());

		var expectedCookies = new HashMap<>(Map.of(
				"sessionID", "abc123",
				"username", "johndoe"
		));
		assertEquals(expectedCookies, request.cookies());
	}
}
