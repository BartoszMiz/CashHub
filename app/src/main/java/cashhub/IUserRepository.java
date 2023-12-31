package cashhub;

import java.util.List;
import java.util.UUID;

public interface IUserRepository {
	User getUserById(UUID id);
	User getUserByEmail(String email);
	List<User> getUsers();
	void addUser(User user);
	void updateUser(User user);
	void deleteUser(UUID id);
}
