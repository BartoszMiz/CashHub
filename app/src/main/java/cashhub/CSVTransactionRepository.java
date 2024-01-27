package cashhub;

import cashhub.logging.ILogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

public class CSVTransactionRepository implements ITransactionRepository {
	private final String savePath;
	private final InMemoryTransactionRepository memoryRepository;
	private final ILogger logger;

	public CSVTransactionRepository(String savePath, ILogger logger) {
		this.savePath = savePath;
		this.logger = logger;
		this.memoryRepository = new InMemoryTransactionRepository();
	}

	@Override
	public List<Transaction> getAllTransactions() {
		return memoryRepository.getAllTransactions();
	}

	@Override
	public List<Transaction> getTransactionInvolvingUser(UUID userId) {
		return memoryRepository.getTransactionInvolvingUser(userId);
	}

	@Override
	public void addTransaction(Transaction transaction) {
		memoryRepository.addTransaction(transaction);
		saveData();
	}

	public void saveData() {
		try {
			var writer = new FileWriter(savePath);

			for (var transaction : memoryRepository.getAllTransactions()) {
				writer.write(String.format("%s,%s,%s,%s,%s\n",
					transaction.id(),
					transaction.senderId(),
					transaction.recipientId(),
					transaction.amount(),
					transaction.executionTime()
				));
			}

			writer.close();
		} catch (IOException e) {
			logger.LogError(String.format("Failed to save transactions to %s: %s", savePath, e.getMessage()));
		}

		logger.LogInformation(String.format("Transactions saved to %s", savePath));
	}

	public void loadData() {
		try {
			var filerReader = new FileReader(savePath);
			var reader = new BufferedReader(filerReader);

			String line;
			while ((line = reader.readLine()) != null) {
				var segments = line.split(",");
				try {
					memoryRepository.addTransaction(new Transaction(
						UUID.fromString(segments[0]),
						UUID.fromString(segments[1]),
						UUID.fromString(segments[2]),
						Double.parseDouble(segments[3]),
						LocalDateTime.parse(segments[4])
					));
				} catch (IndexOutOfBoundsException | IllegalArgumentException | DateTimeParseException e) {
					logger.LogError(String.format("Malformed data in %s", savePath));
				}
			}

			reader.close();
		} catch (IOException e) {
			logger.LogError(String.format("Failed to load transactions from %s: %s", savePath, e.getMessage()));
		}

		logger.LogInformation(String.format("Transaction data loaded from: %s", savePath));
	}
}
