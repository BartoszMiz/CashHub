package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ConsoleLogger;
import cashhub.logging.LogLevel;

public class App {
	private static final int PORT = 8080;

	public static void main(String[] args) {
		var logger = new ConsoleLogger(LogLevel.Information);

		var userRepository = new CSVUserRepository("users.csv", logger);
		userRepository.loadData();

		var transactionRepository = new CSVTransactionRepository("transactions.csv", logger);
		transactionRepository.loadData();

		var authService = new AuthService(userRepository);
		var transactionService = new TransactionService(transactionRepository, userRepository, logger);

		var userService = new UserService(userRepository, authService, transactionRepository, transactionService, logger);

		var router = new Router(logger);
		router.addRoute(HttpVerb.GET, "/", request -> {
			if (authService.getAuthenticatedUser(request) != null) {
				return HttpResponseBuilder.redirectTo("/user/dashboard");
			}

			return HttpResponseBuilder.redirectTo("/index.html");
		});

		router.addRoute(HttpVerb.GET, "/login", request -> {
			if (authService.getAuthenticatedUser(request) != null) {
				return HttpResponseBuilder.redirectTo("/user/dashboard");
			}

			return HttpResponseBuilder.redirectTo("/login.html");
		});

		router.addRoute(HttpVerb.POST, "/user/register", userService::registerUser);
		router.addRoute(HttpVerb.POST, "/user/login", userService::loginUser);
		router.addRoute(HttpVerb.GET, "/user/dashboard", userService::userDashboard);
		router.addRoute(HttpVerb.POST, "/user/logout", userService::logoutUser);
		router.addRoute(HttpVerb.POST, "/user/deposit", userService::deposit);
		router.addRoute(HttpVerb.POST, "/transaction/execute", userService::executeTransaction);

		var httpServer = new HttpServer(PORT, router, logger);
		if (!httpServer.bind()) {
			return;
		}

		while (true) {
			httpServer.processRequest();
		}
	}
}
