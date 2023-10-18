package net.bartek.albatross;

public record HttpRequest(HttpVerb verb, String url, HttpParameters parameters, HttpHeaders headers) {}
