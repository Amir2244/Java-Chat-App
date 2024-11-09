public class UserFactory {
    private UserFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static User createUser(String role, String username, String password, String firstName, String lastName) {
        if (role.equalsIgnoreCase("admin")) {
            return new Admin(username, password, firstName, lastName);
        } else {
            return new RegularUser(username, password, firstName, lastName);
        }
    }
}
