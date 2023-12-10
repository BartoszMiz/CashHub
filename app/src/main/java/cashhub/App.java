package cashhub;

import cashhub.albatross.HttpResponseBuilder;
import cashhub.albatross.HttpServer;
import cashhub.albatross.HttpVerb;
import cashhub.albatross.Router;
import cashhub.logging.ConsoleLogger;
import cashhub.logging.LogLevel;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger(LogLevel.Debug);

		var userRepo = new InMemoryUserRepository();
		var authService = new AuthService();
		var userService = new UserService(userRepo, authService, logger);

		var router = new Router(logger);
		router.addRoute(HttpVerb.GET, "/", request ->
				HttpResponseBuilder.redirectTo("/index.html")
		);

		router.addRoute(HttpVerb.POST, "/user/register", userService::RegisterUser);
		router.addRoute(HttpVerb.POST, "/user/login", userService::LoginUser);

		/*
		 API routes:
		 user/register
		 user/login
		 transfer/schedule
		*/
		// A few routes with templates for the web UI


		var httpServer = new HttpServer(PORT, router, logger);
		if (!httpServer.bind()) {
			return;
		}

		while (true) {
			httpServer.processRequest();
		}
	}
}
