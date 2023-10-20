package net.bartek;

import net.bartek.albatross.HttpServer;
import net.bartek.logging.ConsoleLogger;

public class Main {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger();

		var httpServer = new HttpServer(PORT, logger);
		if (!httpServer.bind()) {
			return;
		}

		while (true) {
			httpServer.processRequest();
		}
	}


}