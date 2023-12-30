package cashhub.albatross;

import java.net.Socket;
import java.util.HashMap;

public record HttpRequest(HttpVerb verb, String url, HttpParameters parameters, HttpHeaders headers, HashMap<String, String> cookies, Socket connection) {}
