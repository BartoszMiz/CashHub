package cashhub.albatross;

public record HttpResponse(HttpStatusCode statusCode, HttpHeaders headers, byte[] content) {
	public byte[] toBytes() {
		var sb = new StringBuilder();
		sb.append(String.format("HTTP/1.1 %s %s\r\n", statusCode.getCode(), statusCode));

		for (var header : headers.value().keySet()) {
			sb.append(String.format("%s: %s\r\n", header, headers.value().get(header)));
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
