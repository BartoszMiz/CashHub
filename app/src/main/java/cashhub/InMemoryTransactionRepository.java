package cashhub;

import java.util.*;

public class InMemoryTransactionRepository implements ITransactionRepository {
	private final List<Transaction> transactions;

	public InMemoryTransactionRepository() {
		transactions = new ArrayList<>();
	}

	@Override
	public List<Transaction> getAllTransactions() {
		return Collections.unmodifiableList(transactions);
	}

	@Override
	public List<Transaction> getTransactionInvolvingUser(UUID userId) {
		return transactions.stream()
			.filter(transaction -> transaction.senderId().equals(userId) || transaction.recipientId().equals(userId))
			.sorted(Comparator.comparing(Transaction::executionTime).reversed())
			.toList();
	}

	@Override
	public void addTransaction(Transaction transaction) {
		transactions.add(transaction);
	}
}
