package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ILogger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserService {
	private final IUserRepository userRepo;
	private final AuthService authService;
	private final ITransactionRepository transactionRepository;
	private final TransactionService transactionService;
	private final ILogger logger;

	public UserService(IUserRepository userRepo, AuthService authService, ITransactionRepository transactionRepository, TransactionService transactionService, ILogger logger) {
		this.userRepo = userRepo;
		this.authService = authService;
		this.transactionRepository = transactionRepository;
		this.transactionService = transactionService;
		this.logger = logger;
	}

	public HttpResponse registerUser(HttpRequest request) {
		var id = UUID.randomUUID();
		var firstName = request.formData().get("firstname");
		var lastName = request.formData().get("lastname");
		var email = request.formData().get("email");
		var password = request.formData().get("password");

		if (firstName == null || lastName == null || email == null || password == null) {
			return HttpResponseBuilder.create()
				.withStatusCode(HttpStatusCode.BadRequest)
				.withContent("firstname, lastname, email or password was not supplied!")
				.build();
		}

		if (userRepo.getUserByEmail(email) != null) {
			return HttpResponseBuilder.create()
				.withStatusCode(HttpStatusCode.Unauthorized)
				.withContent("A user with the supplied email is already registered!")
				.build();
		}

		userRepo.addUser(new User(id, firstName, lastName, email, authService.hashPassword(password), 0));
		logger.LogInformation(String.format("User %s registered", id));
		return HttpResponseBuilder.create().withStatusCode(HttpStatusCode.OK).build();
	}

	public HttpResponse loginUser(HttpRequest request) {
		var email = request.formData().get("email");
		var password = request.formData().get("password");

		if (email == null || password == null) {
			return HttpResponseBuilder.redirectTo("/login.html");
		}

		var passwordHash = authService.hashPassword(password);
		var user = userRepo.getUserByEmail(email);

		if (user == null || !user.passwordHash().equals(passwordHash)) {
			logger.LogWarning(String.format("Unsuccessful login attempt with credentials %s:%s", email, password));
			return HttpResponseBuilder.redirectTo("/login.html");
		}

		logger.LogInformation(String.format("User %s logged in successfully!", user.id()));
		return HttpResponseBuilder.create()
			.withStatusCode(HttpStatusCode.OK)
			.withCookie("authtoken", authService.generateAuthToken(user.id()))
			.withCookie("userid", user.id().toString())
			.addRedirect("/user/dashboard")
			.build();
	}

	public HttpResponse userDashboard(HttpRequest request) {
		var user = authService.getAuthenticatedUser(request);
		if (user == null) {
			return HttpResponseBuilder.redirectTo("/");
		}

		var params = new HashMap<String, String>();
		params.put("full_name", user.firstName() + " " + user.lastName());
		params.put("id", user.id().toString());
		params.put("balance", String.format("%.2f", user.balance()));

		var userTransactions = transactionRepository.getTransactionInvolvingUser(user.id());
		params.put("transactions", generateTransactionsHtml(userTransactions, user.id()));

		try {
			return HttpResponseBuilder.fromTemplate("/user_dashboard.html", params);
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read template user_dashboard.html: %s", e.getMessage()));
			return HttpResponseBuilder.create().withStatusCode(HttpStatusCode.InternalServerError).build();
		}
	}

	public HttpResponse logoutUser(HttpRequest request) {
		return HttpResponseBuilder.create()
			.withCookie("authtoken", "invalid")
			.withCookie("userid", "invalid")
			.addRedirect("/")
			.build();
	}

	public HttpResponse deposit(HttpRequest request) {
		var user = authService.getAuthenticatedUser(request);
		if (user == null) {
			return HttpResponseBuilder.redirectTo("/");
		}

		double depositAmount;
		try {
			depositAmount = Double.parseDouble(request.formData().get("amount"));
		} catch (NullPointerException | NumberFormatException e) {
			return HttpResponseBuilder.create()
				.withStatusCode(HttpStatusCode.BadRequest)
				.withContent("Amount not specified!")
				.build();
		}

		userRepo.updateUserBalance(user, user.balance() + depositAmount);
		return HttpResponseBuilder.redirectTo("/user/dashboard");
	}

	public HttpResponse executeTransaction(HttpRequest request) {
		var sender = authService.getAuthenticatedUser(request);
		if (sender == null) {
			return HttpResponseBuilder.redirectTo("/");
		}

		UUID recipientId;
		double amount;
		try {
			recipientId = UUID.fromString(request.formData().get("recipientId"));
			amount = Double.parseDouble(request.formData().get("amount"));
		} catch (IllegalArgumentException | NullPointerException e) {
			return HttpResponseBuilder.create()
					.withStatusCode(HttpStatusCode.BadRequest)
					.withContent("Recipient ID or amount not specified!")
					.build();
		}

		var currentTime = LocalDateTime.now();
		transactionService.executeTransaction(new Transaction(
			UUID.randomUUID(),
			sender.id(),
			recipientId,
			amount,
			currentTime)
		);

		return HttpResponseBuilder.redirectTo("/user/dashboard");
	}

	private String generateTransactionsHtml(List<Transaction> transactions, UUID userId) {
		var sb = new StringBuilder();
		for (var transaction : transactions) {
			sb.append("<tr>");
			sb.append(String.format("<td>%s</td>", transaction.id()));
			sb.append(String.format("<td>%s</td>", transaction.executionTime().format(DateTimeFormatter.ofPattern("y-MM-dd hh:mm"))));

			var isTransactionOutbound = transaction.senderId().equals(userId);
			if (isTransactionOutbound) {
				sb.append(String.format("<td style=\"color:red\">-&dollar;%.2f</td>", transaction.amount()));
			} else {
				sb.append(String.format("<td style=\"color:green\">+&dollar;%.2f</td>", transaction.amount()));
			}

			sb.append("</tr>");
		}

		return sb.toString();
	}
}
