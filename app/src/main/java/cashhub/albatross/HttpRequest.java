package cashhub.albatross;

public record HttpRequest(HttpVerb verb, String url, HttpParameters parameters, HttpHeaders headers) {}
