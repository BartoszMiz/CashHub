package net.bartek;

import java.util.HashMap;

public record HttpRequest(HttpVerb verb, String url,HttpParameters parameters, HttpHeaders headers) {}
