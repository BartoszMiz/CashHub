package cashhub;

import java.util.List;
import java.util.UUID;

public interface IUserRepository {
	User getUser(UUID id);
	User getUser(String email);
	List<User> getUsers();
	void addUser(User user);
	void updateUser(User user);
	void deleteUser(UUID id);
}
