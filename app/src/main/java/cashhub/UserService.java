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

	public HttpResponse RegisterUser(HttpRequest request) {
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

		if (userRepo.GetUser(email) != null) {
			return HttpResponseBuilder.create()
					.withStatusCode(HttpStatusCode.Unauthorized)
					.withContent("A user with the supplied email is already registered!")
					.build();
		}

		userRepo.AddUser(new User(id, firstName, lastName, email, HashPassword(password)));
		return HttpResponseBuilder.create().withStatusCode(HttpStatusCode.OK).build();
	}

	public HttpResponse LoginUser(HttpRequest request) {
		var email = request.parameters().value().get("email");
		var password = request.parameters().value().get("password");

		if (email == null || password == null) {
			return HttpResponseBuilder.create()
					.withStatusCode(HttpStatusCode.BadRequest)
					.withContent("email or password not supplied!")
					.build();
		}

		var passwordHash = HashPassword(password);
		var user = userRepo.GetUser(email);

		if (user == null || !user.passwordHash().equals(passwordHash)) {
			return HttpResponseBuilder.create()
					.withStatusCode(HttpStatusCode.Unauthorized)
					.withContent("Wrong email or password!")
					.build();
		}

		return HttpResponseBuilder.create()
				.withStatusCode(HttpStatusCode.OK)
				.withContent(String.format("\"token\":\"%s\"", authService.GenerateAuthToken(user.id())))
				.build();
	}

	private String HashPassword(String password) {
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
