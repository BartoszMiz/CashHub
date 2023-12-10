package cashhub;

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class AuthService {
	private final String encryptionPassphrase = "super-omega-secure-passphrase";

	public String GenerateAuthToken(UUID userId) {
		var data = userId.toString().getBytes();
		var encryptedData = Encrypt(data, encryptionPassphrase.getBytes());
		return Arrays.toString(Base64.getEncoder().encode(encryptedData));
	}

	public boolean ValidateAuthToken(String token, UUID userId) {
		var encryptedData = Base64.getDecoder().decode(token);
		var data = Arrays.toString(Encrypt(encryptedData, encryptionPassphrase.getBytes()));
		return data.equals(userId.toString());
	}

	private byte[] Encrypt(byte[] data, byte[] passphrase) {
		var result = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (byte)(data[i] ^ passphrase[i % passphrase.length]);
		}
		return result;
	}
}
