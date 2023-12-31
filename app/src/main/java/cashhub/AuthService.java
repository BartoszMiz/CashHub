package cashhub;

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class AuthService {
	private final String encryptionPassphrase = "super-omega-secure-passphrase";

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

	// XOR == encryption LOL
	private byte[] encrypt(byte[] data, byte[] passphrase) {
		var result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte)(data[i] ^ passphrase[i % passphrase.length]);
		}
		return result;
	}
}
