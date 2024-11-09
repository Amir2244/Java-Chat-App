import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {
    private static final Logger LOGGER = Logger.getLogger(ClientCallbackImpl.class.getName());
    private final String username;

    public ClientCallbackImpl(String username) throws RemoteException {
        this.username = username;
        LOGGER.info(() -> String.format("Created new ClientCallback for user: %s", username));
    }

    @Override
    public void onMessageReceived(Message message) throws RemoteException {
        LOGGER.info(() -> String.format("New message received from %s: %s", message.getSender(), message.getContent()));
    }

    @Override
    public void onRoomMessageReceived(String roomName, Message message) throws RemoteException {
        LOGGER.info(() -> String.format("New room message received in %s from %s: %s", roomName, message.getSender(), message.getContent()));
    }

    @Override
    public void onUserStatusChanged(String username, boolean online) throws RemoteException {
        String status = online ? "online" : "offline";
        LOGGER.info(() -> String.format("User status changed: %s is now %s", username, status));
    }

    @Override
    public void onFileReceived(Message fileMessage) throws RemoteException {
        LOGGER.info(() -> String.format("New file received from %s: %s", fileMessage.getSender(), fileMessage.getFileName()));
    }

    @Override
    public void onRoomFileReceived(String roomName, Message fileMessage) throws RemoteException {
        LOGGER.info(() -> String.format("New file received in room %s from %s: %s", roomName, fileMessage.getSender(), fileMessage.getFileName()));
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClientCallbackImpl other = (ClientCallbackImpl) obj;
        return this.username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}