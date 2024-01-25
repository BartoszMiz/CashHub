package cashhub;

import java.util.List;
import java.util.UUID;

public interface ITransactionRepository {
	List<Transaction> getAllTransactions();
	List<Transaction> getTransactionInvolvingUser(UUID userId);
	void addTransaction(Transaction transaction);
}
