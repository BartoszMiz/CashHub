package cashhub.albatross;

import java.util.Map;

public record HttpResponse(HttpStatusCode statusCode, Map<String, String> headers, Map<String, String> cookies, byte[] content) {
	public byte[] toBytes() {
		var sb = new StringBuilder();
		sb.append(String.format("HTTP/1.1 %s %s\r\n", statusCode.getCode(), statusCode));

		for (var headerName : headers.keySet()) {
			sb.append(String.format("%s: %s\r\n", headerName, headers.get(headerName)));
		}

		for (var cookieName : cookies.keySet()) {
			sb.append(String.format("Set-Cookie: %s=%s; Path=/\r\n", cookieName, cookies.get(cookieName)));
		}

		int contentLength = 0;
		if (content != null) {
			contentLength = content.length;
		}
		sb.append(String.format("Content-Length: %s\r\n", contentLength));
		sb.append("\r\n");

		var headerBytes = sb.toString().getBytes();
		var bytes = new byte[headerBytes.length + contentLength];

		System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
		if (content != null) {
			System.arraycopy(content, 0, bytes, headerBytes.length, contentLength);
		}

		return bytes;
	}
}
