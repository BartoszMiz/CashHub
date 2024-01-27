package cashhub;

import cashhub.logging.ILogger;

public class TransactionService {
	private final ITransactionRepository transactionRepository;
	private final IUserRepository userRepository;
	private final ILogger logger;

	public TransactionService(ITransactionRepository transactionRepository, IUserRepository userRepository, ILogger logger) {
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
		this.logger = logger;
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

		logger.LogInformation(String.format(
			"Executed transaction of $%.2f from %s to %s",
			transaction.amount(),
			transaction.senderId(),
			transaction.recipientId()
		));

		return true;
	}
}
