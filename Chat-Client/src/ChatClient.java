import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ChatClient {
    private static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());
    private ChatService chatService;
    private final String host;
    private final int port;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            chatService = (ChatService) registry.lookup("ChatService");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error connecting to the server", e);
        }
    }

    public boolean signIn(String username, String password) throws RemoteException {
        boolean success = chatService.signIn(username, password);
        if (success) {
            LOGGER.log(Level.INFO, "User {0} signed in successfully", username);
            ClientCallback clientCallback = new ClientCallbackImpl(username);
            registerCallback(username, clientCallback);
            return true;
        } else {
            LOGGER.log(Level.WARNING, "Sign-in failed for user {0}", username);
            return false;
        }
    }

    public boolean connectivityCheck() {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.lookup("ChatService");
            LOGGER.log(Level.INFO, "Connectivity check successful");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Connectivity check failed", e);
            return false;
        }
    }

    public void registerCallback(String username, ClientCallback callback) throws RemoteException {
        chatService.registerCallback(username, callback);
        LOGGER.log(Level.INFO, "Callback registered for user {0}", username);
    }

    public void signUp(String username, String password, String firstName, String lastName) throws RemoteException {
        chatService.signUp(username, password, firstName, lastName);
        LOGGER.log(Level.INFO, "New user signed up: {0}", username);
    }

    public boolean joinRoom(String username, String roomName) throws RemoteException {
        boolean success = chatService.joinRoom(username, roomName);
        LOGGER.log(Level.INFO, "User {0} {1} room {2}", new Object[]{username, success ? "joined" : "failed to join", roomName});
        return success;
    }

    public void addChatRoom(String roomName) throws RemoteException {
        chatService.addChatRoom(roomName);
        LOGGER.log(Level.INFO, "Chat room created: {0} by {1}", new Object[]{roomName});
    }

    public void deleteRoom(String username, String roomName) throws RemoteException {
        chatService.deleteRoom(username, roomName);
        LOGGER.log(Level.INFO, "Chat room {0} deleted by {1}", new Object[]{roomName, username});
    }


    public void unregisterCallback(String username) throws RemoteException {
        chatService.unregisterCallback(username);
        LOGGER.log(Level.INFO, "Callback unregistered for user {0}", username);
    }

    public String[] getOnlineUsers() throws RemoteException {
        String[] users = chatService.getOnlineUsers();
        LOGGER.log(Level.FINE, "Retrieved online users list. Count: {0}", users.length);
        return users;
    }

    public void sendMessage(String sender, String recipient, String message) throws RemoteException {
        Command sendMessageCommand = new SendMessageCommand(chatService, sender, recipient, message);
        sendMessageCommand.execute();
        LOGGER.log(Level.FINE, "Message sent from {0} to {1}", new Object[]{sender, recipient});
    }

    public boolean leaveRoom(String username, String roomName) throws RemoteException {
        boolean success = chatService.leaveRoom(username, roomName);
        LOGGER.log(Level.INFO, "User {0} {1} room {2}", new Object[]{username, success ? "left" : "failed to leave", roomName});
        return success;
    }

    public void sendMessageToRoom(String sender, String roomName, String message) throws RemoteException {
        chatService.sendMessageToRoom(sender, roomName, message);
        LOGGER.log(Level.FINE, "Message sent from {0} to room {1}", new Object[]{sender, roomName});
    }

    public String[] getAvailableRooms() throws RemoteException {
        String[] rooms = chatService.getAvailableRooms();
        LOGGER.log(Level.FINE, "Retrieved available rooms list. Count: {0}", rooms.length);
        return rooms;
    }

    public List<Message> getMessageHistory(String currentUser) throws RemoteException {
        return chatService.getMessageHistory(currentUser);
    }

    public String[] getRoomClients(String selectedRoom) throws RemoteException {
        String[] clients = chatService.getRoomClients(selectedRoom);
        LOGGER.log(Level.FINE, "Retrieved {0} clients for room {1}",
                new Object[]{clients.length, selectedRoom});
        return clients;
    }

    public void updateRoomClients(String roomName) throws RemoteException {
        chatService.updateRoomClients(roomName);
        LOGGER.log(Level.FINE, "Updated clients for room {0}", roomName);
    }


    public void sendFile(String sender, String recipient, byte[] fileData, String fileName) throws RemoteException {
        chatService.sendFile(sender, recipient, fileData, fileName);
        LOGGER.log(Level.FINE, "File sent from {0} to {1}", new Object[]{sender, recipient});
    }

    public void sendFileToRoom(String sender, String roomName, byte[] fileData, String fileName) throws RemoteException {
        chatService.sendFileToRoom(sender, roomName, fileData, fileName);
        LOGGER.log(Level.FINE, "File sent from {0} to room {1}", new Object[]{sender, roomName});
    }

    public boolean isUserInRoom(String username, String roomName) throws RemoteException {
        return chatService.isUserInRoom(username, roomName);
    }

    public void signOut(String username) throws RemoteException {
        chatService.signOut(username);
        LOGGER.log(Level.INFO, "User {0} signed out", username);
    }

    public boolean canDeleteRoom(String currentUser, String selectedRoom) {
        try {
            return chatService.canDeleteRoom(currentUser, selectedRoom);
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user can delete room", e);
            return false;
        }
    }

}