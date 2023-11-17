package cashhub.albatross;

// https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
public enum HttpStatusCode {
	OK(200),
	Created(201),
	Found(302),
	BadRequest(400),
	Unauthorized(401),
	Forbidden(403),
	NotFound(404),
	MethodNotAllowed(405),
	ImATeapot(418),
	TooManyRequests(429),
	InternalServerError(500);


	private final int code;

	HttpStatusCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
