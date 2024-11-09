import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void onFileReceived(Message fileMessage) throws RemoteException;
    void onRoomFileReceived(String roomName, Message fileMessage) throws RemoteException;
    void onMessageReceived(Message message) throws RemoteException;
    void onRoomMessageReceived(String roomName, Message message) throws RemoteException;
    void onUserStatusChanged(String username, boolean online) throws RemoteException;
}
