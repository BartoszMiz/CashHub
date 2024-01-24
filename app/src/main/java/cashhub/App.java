package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ConsoleLogger;
import cashhub.logging.LogLevel;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger(LogLevel.Debug);

		var userRepo = new CSVUserRepository("users.csv", logger);
		userRepo.loadData();

		var authService = new AuthService();
		var userService = new UserService(userRepo, authService, logger);

		var router = new Router(logger);
		router.addRoute(HttpVerb.GET, "/", request ->
				HttpResponseBuilder.redirectTo("/index.html")
		);

		router.addRoute(HttpVerb.POST, "/user/register", userService::registerUser);
		router.addRoute(HttpVerb.POST, "/user/login", userService::loginUser);
		router.addRoute(HttpVerb.GET, "/user/dashboard", userService::userDashboard);
		router.addRoute(HttpVerb.POST, "/user/logout", userService::logoutUser);
		router.addRoute(HttpVerb.POST, "/user/deposit", userService::deposit);

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
