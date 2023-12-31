package cashhub.albatross;

public class MalformedHttpRequestException extends IllegalArgumentException {
	public MalformedHttpRequestException(String message, Exception e) {
		super(message, e);
	}

	public MalformedHttpRequestException(String message) {
		super(message);
	}
}
