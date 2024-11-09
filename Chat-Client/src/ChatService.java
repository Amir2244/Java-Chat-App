import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {
    boolean signIn(String username, String password) throws RemoteException;

    void signOut(String username) throws RemoteException;

    void signUp(String username, String password, String firstName, String lastName) throws RemoteException;

    void sendMessage(String sender, String recipient, String message) throws RemoteException;

    void sendMessageToRoom(String sender, String roomName, String message) throws RemoteException;

    void addChatRoom(String roomName) throws RemoteException;

    void deleteRoom(String username, String roomName) throws RemoteException;

    void registerCallback(String username, ClientCallback callback) throws RemoteException;

    void unregisterCallback(String username) throws RemoteException;

    boolean joinRoom(String username, String roomName) throws RemoteException;


    boolean leaveRoom(String username, String roomName) throws RemoteException;
    void updateRoomClients(String roomName) throws RemoteException;

    boolean isUserInRoom(String username, String roomName) throws RemoteException;
    boolean canDeleteRoom(String username, String roomName) throws RemoteException;
    List<Message> getMessageHistory(String signedInUser) throws RemoteException;

    String[] getRoomClients(String roomName) throws RemoteException;

    String[] getOnlineUsers() throws RemoteException;

    String[] getAvailableRooms() throws RemoteException;

    void sendFile(String sender, String recipient, byte[] fileData, String fileName) throws RemoteException;

    void sendFileToRoom(String sender, String roomName, byte[] fileData, String fileName) throws RemoteException;
}
