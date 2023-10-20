package net.bartek.albatross;

import java.net.Socket;

public record HttpRequest(HttpVerb verb, String url, HttpParameters parameters, HttpHeaders headers, Socket connection) {}
