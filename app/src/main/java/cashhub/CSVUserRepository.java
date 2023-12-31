package cashhub;

import cashhub.logging.ILogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CSVUserRepository implements IUserRepository {
	private final InMemoryUserRepository memoryRepo;
	private final String savePath;
	private final ILogger logger;

	public CSVUserRepository(String savePath, ILogger logger) {
		memoryRepo = new InMemoryUserRepository();
		this.savePath = savePath;
		this.logger = logger;
	}

	@Override
	public User getUserById(UUID id) {
		return memoryRepo.getUserById(id);
	}

	@Override
	public User getUserByEmail(String email) {
		return memoryRepo.getUserByEmail(email);
	}

	@Override
	public List<User> getUsers() {
		return memoryRepo.getUsers();
	}

	@Override
	public void addUser(User user) {
		memoryRepo.addUser(user);
		saveData();
	}

	@Override
	public void updateUser(User user) {
		memoryRepo.updateUser(user);
		saveData();
	}

	@Override
	public void deleteUser(UUID id) {
		memoryRepo.deleteUser(id);
		saveData();
	}

	public void loadData() {
		try {
			var fileReader = new FileReader(savePath);
			var reader = new BufferedReader(fileReader);

			String line;
			while ((line = reader.readLine()) != null) {
				var segments = line.split(",");
				try {
					memoryRepo.addUser(new User(UUID.fromString(segments[0]), segments[1], segments[2], segments[3], segments[4]));
				} catch (IndexOutOfBoundsException | IllegalArgumentException e) {
					logger.LogError("Malformed data in the save file!");
				}
			}

			reader.close();
		} catch (IOException e) {
			logger.LogError(String.format("Failed to read from save file: %s", e.getMessage()));

		}

		logger.LogInformation(String.format("Data loaded from %s", savePath));
	}

	public void saveData() {
		try {
			var writer = new FileWriter(savePath);

			for (var user : memoryRepo.getUsers()) {
				writer.write(String.format("%s,%s,%s,%s,%s\n", user.id(), user.firstName(), user.lastName(), user.email(), user.passwordHash()));
			}

			writer.close();
		} catch (IOException e) {
			logger.LogError(String.format("Failed to write to save file: %s", e.getMessage()));
		}

		logger.LogInformation(String.format("Data saved to %s", savePath));
	}
}
