public class RegularUser extends User {

    public RegularUser(String username, String password, String firstName, String lastName) {
        super(username, password, firstName, lastName);
    }

    @Override
    public void sendMessage(String message, ChatRoom room) {
        room.broadcastMessage(this, message);
    }
}
