package cashhub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryUserRepository implements IUserRepository {
	private final ArrayList<User> users;

	public InMemoryUserRepository() {
		users = new ArrayList<>();
	}

	@Override
	public User getUserById(UUID id) {
		for (var user : users) {
			if (user.id().equals(id)) {
				return user;
			}
		}
		return null;
	}

	@Override
	public User getUserByEmail(String email) {
		for (var user : users) {
			if (user.email().equals(email)) {
				return user;
			}
		}
		return null;
	}

	public List<User> getUsers() {
		return users;
	}

	@Override
	public void addUser(User user) {
		if (getUserById(user.id()) != null) {
			return;
		}
		users.add(user);
	}

	@Override
	public void updateUser(User user) {
		var oldUser = getUserById(user.id());
		if (oldUser == null) {
			return;
		}

		users.remove(oldUser);
		users.add(user);
	}

	@Override
	public void deleteUser(UUID id) {
		var user = getUserById(id);
		users.remove(user);
	}
}
