package cashhub;

import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(UUID id, UUID senderId, UUID recipientId, double amount, LocalDateTime executionTime) {}
