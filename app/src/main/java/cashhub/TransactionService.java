package cashhub;

import cashhub.albatross.HttpRequest;
import cashhub.albatross.HttpResponse;
import cashhub.albatross.HttpResponseBuilder;
import cashhub.albatross.HttpStatusCode;
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

	public HttpResponse executeTransaction(HttpRequest request) {
		// TODO: Implement transaction handling
		return HttpResponseBuilder.create()
			.withStatusCode(HttpStatusCode.MethodNotAllowed)
			.withContent("Not yet implemented :(")
			.build();
	}
}
