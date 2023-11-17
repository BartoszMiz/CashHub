package cashhub.albatross;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class TemplateProcessor {
	public static String processTemplate(Path templatePath, HashMap<String, String> parameters) throws IOException {
		var template = Files.readString(templatePath);

		for (var paramName : parameters.keySet()) {
			template = template.replaceAll(String.format("\\{\\{%s\\}\\}", paramName), parameters.get(paramName));
		}

		return template;
	}
}
