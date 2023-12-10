package cashhub;

import java.util.UUID;

public interface IUserRepository {
	User	GetUser(UUID id);
	User GetUser(String email);
	void AddUser(User user);
	void UpdateUser(User user);
	void DeleteUser(UUID id);
}
