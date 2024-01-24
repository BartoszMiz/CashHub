package cashhub;

import java.util.Objects;
import java.util.UUID;

public final class User {
	private final UUID id;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final String passwordHash;
	private double balance;

	public User(UUID id, String firstName, String lastName, String email, String passwordHash, double balance) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.passwordHash = passwordHash;
		this.balance = balance;
	}

	public UUID getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double newBalance) {
		balance = newBalance;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (User) obj;
		return Objects.equals(this.id, that.id) &&
				Objects.equals(this.firstName, that.firstName) &&
				Objects.equals(this.lastName, that.lastName) &&
				Objects.equals(this.email, that.email) &&
				Objects.equals(this.passwordHash, that.passwordHash) &&
				Double.doubleToLongBits(this.balance) == Double.doubleToLongBits(that.balance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, firstName, lastName, email, passwordHash, balance);
	}

	@Override
	public String toString() {
		return "User[" +
			"id=" + id + ", " +
			"firstName=" + firstName + ", " +
			"lastName=" + lastName + ", " +
			"email=" + email + ", " +
			"passwordHash=" + passwordHash + ", " +
			"balance=" + balance + ']';
	}

}
