package net.bartek;

import java.util.HashMap;

public record HttpHeaders(HashMap<String, String> value) { }
