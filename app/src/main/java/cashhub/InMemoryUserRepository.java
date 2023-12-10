package cashhub;

import java.util.ArrayList;
import java.util.UUID;

public class InMemoryUserRepository implements IUserRepository {
	private final ArrayList<User> users;

	public InMemoryUserRepository() {
		users = new ArrayList<>();
	}

	@Override
	public User GetUser(UUID id) {
		for (var user : users) {
			if (user.id().equals(id)) {
				return user;
			}
		}
		return null;
	}

	@Override
	public User GetUser(String email) {
		for (var user : users) {
			if (user.email().equals(email)) {
				return user;
			}
		}
		return null;
	}

	@Override
	public void AddUser(User user) {
		if (GetUser(user.id()) != null) {
			return;
		}
		users.add(user);
	}

	@Override
	public void UpdateUser(User user) {
		var oldUser = GetUser(user.id());
		if (oldUser == null) {
			return;
		}

		users.remove(oldUser);
		users.add(user);
	}

	@Override
	public void DeleteUser(UUID id) {
		var user = GetUser(id);
		users.remove(user);
	}
}
