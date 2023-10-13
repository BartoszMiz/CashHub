package net.bartek.logging;

public interface ILogger {
	public void Log(LogLevel level, String message);

	public void LogDebug(String message);
	public void LogInformation(String message);
	public void LogWarning(String message);
	public void LogError(String message);
}
