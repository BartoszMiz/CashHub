package cashhub;

import cashhub.albatross.HttpRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class AuthService {
	private final IUserRepository userRepository;
	private final String encryptionPassphrase = "super-omega-secure-passphrase";

	public AuthService(IUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public String generateAuthToken(UUID userId) {
		var data = userId.toString().getBytes();
		var encryptedData = encrypt(data, encryptionPassphrase.getBytes());

		return new String(Base64.getEncoder().encode(encryptedData));
	}

	public boolean validateAuthToken(String token, UUID userId) {
		var encryptedData = Base64.getDecoder().decode(token);
		var data = encrypt(userId.toString().getBytes(), encryptionPassphrase.getBytes());
		return Arrays.equals(data, encryptedData);
	}

	public User getAuthenticatedUser(HttpRequest request) {
		var authToken = request.cookies().get("authtoken");
		var userIdString = request.cookies().get("userid");

		if (authToken == null || userIdString == null) {
			return null;
		}

		UUID userId;
		try {
			userId = UUID.fromString(userIdString);
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (validateAuthToken(authToken, userId)) {
			return userRepository.getUserById(userId);
		}

		return null;
	}

	public String hashPassword(String password) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ignored) {
			return "UNREACHABLE";
		}

		var hashBytes = messageDigest.digest(password.getBytes());
		return new String(Base64.getEncoder().encode(hashBytes));
	}

	// XOR == encryption LOL
	private byte[] encrypt(byte[] data, byte[] passphrase) {
		var result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte)(data[i] ^ passphrase[i % passphrase.length]);
		}
		return result;
	}
}
