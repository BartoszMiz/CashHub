package cashhub;

import cashhub.albatross.*;
import cashhub.logging.ILogger;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class UserService {
	private final IUserRepository userRepo;
	private final AuthService authService;
	private final ILogger logger;

	public UserService(IUserRepository userRepo, AuthService authService, ILogger logger) {
		this.userRepo = userRepo;
		this.authService = authService;
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

		userRepo.addUser(new User(id, firstName, lastName, email, hashPassword(password)));
		logger.LogInformation(String.format("User %s registered", id));
		return HttpResponseBuilder.create().withStatusCode(HttpStatusCode.OK).build();
	}

	public HttpResponse loginUser(HttpRequest request) {
		var email = request.formData().get("email");
		var password = request.formData().get("password");

		if (email == null || password == null) {
			return HttpResponseBuilder.redirectTo("/login.html");
		}

		var passwordHash = hashPassword(password);
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
		var authToken = request.cookies().get("authtoken");
		var userId = request.cookies().get("userid");
		if (authToken == null || userId == null || !authService.validateAuthToken(authToken, UUID.fromString(userId))) {
			return HttpResponseBuilder.redirectTo("/");
		}

		var user = userRepo.getUserById(UUID.fromString(userId));
		var params = new HashMap<String, String>();
		params.put("full_name", user.firstName() + " " + user.lastName());
		params.put("id", userId);
		params.put("balance", "420.69");

		try {
			return HttpResponseBuilder.fromTemplate("/user_dashboard.html", params);
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read template user_dashboard.html: %s", e.getMessage()));
			return HttpResponseBuilder.create().withStatusCode(HttpStatusCode.InternalServerError).build();
		}
	}

	private String hashPassword(String password) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ignored) {
			return "UNREACHABLE";
		}

		var hashBytes = messageDigest.digest(password.getBytes());
		return new String(Base64.getEncoder().encode(hashBytes));
	}
}
