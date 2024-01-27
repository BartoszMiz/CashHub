package cashhub;

public class TransactionService {
	private final ITransactionRepository transactionRepository;
	private final IUserRepository userRepository;

	public TransactionService(ITransactionRepository transactionRepository, IUserRepository userRepository) {
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
	}

	public boolean executeTransaction(Transaction transaction) {
		if (transaction.senderId() == null | transaction.recipientId() == null || transaction.senderId().equals(transaction.recipientId())) {
			return false;
		}

		var sender = userRepository.getUserById(transaction.senderId());
		var recipient = userRepository.getUserById(transaction.recipientId());

		if (sender == null || recipient == null) {
			return false;
		}

		if (transaction.amount() <= 0) {
			return false;
		}

		if (sender.balance() < transaction.amount()) {
			return false;
		}

		userRepository.updateUserBalance(sender, sender.balance() - transaction.amount());
		userRepository.updateUserBalance(recipient, recipient.balance() + transaction.amount());
		transactionRepository.addTransaction(transaction);
		return true;
	}
}
