import java.io.Serializable;

public abstract class User implements Serializable {
    protected String username;
    protected String password;
    protected String firstName;
    protected String lastName;

    protected User(String username, String password, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public abstract void sendMessage(String message, ChatRoom room);
}