import java.rmi.RemoteException;

public class Admin extends User {

    public Admin(String username, String password, String firstName, String lastName) {
        super(username, password, firstName, lastName);
    }

    @Override
    public void sendMessage(String message, ChatRoom room) {
        room.broadcastMessage(this, message);
    }

    public void blockUser(User user, ChatRoom room) {
        room.blockClient(user);
    }

}
