package cashhub;

import cashhub.albatross.HttpRequest;
import cashhub.albatross.HttpResponse;
import cashhub.albatross.HttpResponseBuilder;
import cashhub.albatross.HttpStatusCode;
import cashhub.logging.ILogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
		var firstName = request.parameters().value().get("firstname");
		var lastName = request.parameters().value().get("lastname");
		var email = request.parameters().value().get("email");
		var password = request.parameters().value().get("password");

		if (firstName == null || lastName == null || email == null || password == null) {
			return HttpResponseBuilder.create()
					.withStatusCode(HttpStatusCode.BadRequest)
					.withContent("firstname, lastname, email or password was not supplied!")
					.build();
		}

		if (userRepo.getUser(email) != null) {
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
		var email = request.parameters().value().get("email");
		var password = request.parameters().value().get("password");

		if (email == null || password == null) {
			return HttpResponseBuilder.create()
					.withStatusCode(HttpStatusCode.BadRequest)
					.withContent("email or password not supplied!")
					.build();
		}

		var passwordHash = hashPassword(password);
		var user = userRepo.getUser(email);

		if (user == null || !user.passwordHash().equals(passwordHash)) {
			logger.LogWarning(String.format("Unsuccessful login attempt with credentials %s:%s", email, password));
			return HttpResponseBuilder.create()
					.withStatusCode(HttpStatusCode.Unauthorized)
					.withContent("Wrong email or password!")
					.build();
		}

		logger.LogInformation(String.format("User %s logged in successfully!", user.id()));
		return HttpResponseBuilder.create()
				.withStatusCode(HttpStatusCode.OK)
				.withContent(String.format("\"token\":\"%s\"", authService.generateAuthToken(user.id())))
				.build();
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
