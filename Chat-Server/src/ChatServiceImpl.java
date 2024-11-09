import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public final class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private final transient DbContext dbHelper;
    private final transient ConcurrentHashMap<String, ClientCallback> connectedClients;
    private final ConcurrentHashMap<String, ChatRoom> chatRooms;
    private final ConcurrentHashMap<String, User> userProfiles;
    private final transient Logger logger = Logger.getInstance();
    private static final String USER_ENTITY = "User ";

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        ChatServiceImpl that = (ChatServiceImpl) obj;
        return Objects.equals(chatRooms, that.chatRooms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), chatRooms);
    }

    public ChatServiceImpl() throws RemoteException {
        logger.info("Initializing ChatServiceImpl");
        dbHelper = DbContext.getInstance("chat_app", "root", "amirouv@#$%4321");
        connectedClients = new ConcurrentHashMap<>();
        chatRooms = new ConcurrentHashMap<>();
        userProfiles = new ConcurrentHashMap<>();

        for (ChatRoom room : dbHelper.getAllRooms()) {
            chatRooms.put(room.getName(), room);
        }

        logger.info("ChatServiceImpl initialized successfully");
    }

    @Override
    public boolean signIn(String username, String password) throws RemoteException {
        logger.info("Sign in attempt for user: " + username);

        if (dbHelper.authenticateUser(username, password)) {
            User user = dbHelper.getUser(username);
            if (user != null) {
                userProfiles.put(username, user);
                notifyUserStatus(username, true);
                logger.info("User successfully signed in: " + username);
                return true;
            }
        }

        logger.warn("Failed sign in attempt for user: " + username);
        return false;
    }

    @Override
    public void signOut(String username) throws RemoteException {
        logger.info("Sign out attempt for user: " + username);
        if (connectedClients.containsKey(username)) {
            connectedClients.remove(username);
            notifyUserStatus(username, false);
            logger.info("User successfully signed out: " + username);
        } else {
            logger.warn("Failed sign out attempt for user: " + username);
        }
    }


    @Override
    public void signUp(String username, String password, String firstName, String lastName) throws RemoteException {
        logger.info("New " + USER_ENTITY.toLowerCase() + " registration attempt: " + username);
        User newUser = UserFactory.createUser("regular", username, password, firstName, lastName);
        if (dbHelper.addUser(newUser)) {
            userProfiles.put(username, newUser);
            logger.info(USER_ENTITY + " registered successfully: " + username);
        } else {
            logger.error("Failed to register " + USER_ENTITY.toLowerCase() + ": " + username);
        }
    }

    @Override
    public void sendMessage(String sender, String recipient, String content) throws RemoteException {
        logger.info("Message sending attempt from " + sender + " to " + recipient);
        User senderUser = dbHelper.getUser(sender);
        User recipientUser = dbHelper.getUser(recipient);

        if (senderUser != null && recipientUser != null) {
            Message chatMessage = new Message(sender, recipient, content, System.currentTimeMillis());
            dbHelper.saveMessage(chatMessage);

            ClientCallback recipientCallback = connectedClients.get(recipient);
            if (recipientCallback != null) {
                recipientCallback.onMessageReceived(chatMessage);
                logger.info("Message delivered successfully from " + sender + " to " + recipient);
            } else {
                logger.warn("Recipient offline, message stored: " + recipient);
            }
        } else {
            logger.error("Message sending failed - invalid users: " + sender + " -> " + recipient);
        }
    }


    @Override
    public void registerCallback(String username, ClientCallback callback) {
        logger.info("Registering callback for user: " + username);
        connectedClients.put(username, callback);
        notifyUserStatus(username, true);
        try {
            for (String existingUser : connectedClients.keySet()) {
                if (!existingUser.equals(username)) {
                    callback.onUserStatusChanged(existingUser, true);
                }
            }
            logger.info("Callback registered successfully for: " + username);
        } catch (RemoteException e) {
            connectedClients.remove(username);
            logger.error("Callback registration failed for " + username + ": " + e.getMessage());
        }
    }

    @Override
    public void unregisterCallback(String username) throws RemoteException {
        logger.info("Unregistering callback for user: " + username);
        connectedClients.remove(username);
        userProfiles.remove(username);
        notifyUserStatus(username, false);
        logger.info("Callback unregistered for user: " + username);
    }



    @Override
    public boolean joinRoom(String username, String roomName) throws RemoteException {
        logger.info(USER_ENTITY + username + " attempting to join room: " + roomName);
        if (chatRooms.containsKey(roomName)) {
            User user = userProfiles.get(username);
            if (user != null && !dbHelper.isUserInRoom(username, roomName)) {
                chatRooms.get(roomName).addClient(user);
                dbHelper.saveRoomMember(username, roomName);
                logger.info(USER_ENTITY + username + " joined room: " + roomName);
                return true;
            }
            if (dbHelper.isUserInRoom(username, roomName)) {
                logger.info(USER_ENTITY + username + " already in room: " + roomName);
                return true;
            }
        }
        logger.warn("Failed to join room " + roomName + " - room not found or user invalid");
        return false;
    }



    @Override
    public boolean leaveRoom(String username, String roomName) throws RemoteException {
        logger.info(USER_ENTITY + username + " leaving room: " + roomName);
        if (chatRooms.containsKey(roomName)) {
            User user = userProfiles.get(username);
            if (user != null) {
                chatRooms.get(roomName).removeClient(user);
                logger.info(USER_ENTITY + username + " left room: " + roomName);
                return true;
            }
        }
        logger.warn("Failed to leave room " + roomName + " - room not found");
        return false;
    }

    @Override
    public boolean isUserInRoom(String username, String roomName) throws RemoteException {
        logger.info("Checking if " + USER_ENTITY + username + " is in room: " + roomName);
        if (chatRooms.containsKey(roomName)) {
            User user = userProfiles.get(username);
            if (user != null) {
                return chatRooms.get(roomName).isUserInRoom(user);
            }
        }
        logger.warn("Failed to check user status in room " + roomName + " - room not found");
        return false;
    }


    @Override
    public List<Message> getMessageHistory(String signedInUser) throws RemoteException {
        logger.info("Fetching message history for user: " + signedInUser);
        return dbHelper.getMessageHistory(signedInUser);
    }

    @Override
    public void addChatRoom(String roomName) throws RemoteException {
        logger.info("Creating new chat room: " + roomName);
        ChatRoom newRoom = new ChatRoom(roomName);
        chatRooms.putIfAbsent(roomName, newRoom);
        dbHelper.saveRoom(newRoom);
        logger.info("Chat room created successfully: " + roomName);
    }

    @Override
    public void sendMessageToRoom(String sender, String roomName, String content) throws RemoteException {
        logger.info("Room message attempt from " + sender + " to room: " + roomName);
        if (chatRooms.containsKey(roomName)) {
            ChatRoom room = chatRooms.get(roomName);
            User senderUser = userProfiles.get(sender);

            if (senderUser != null) {
                Message message = new Message(sender, roomName, content, System.currentTimeMillis());
                message.setRoomMessage(true);
                dbHelper.saveRoomMessage(message, roomName);
                room.broadcastMessage(senderUser, message.getContent());
                for (User client : room.getClients()) {
                    ClientCallback callback = connectedClients.get(client.getUsername());
                    if (callback != null) {
                        callback.onRoomMessageReceived(roomName, message);
                        logger.info("Room message delivered to: " + client.getUsername());
                    }
                }
                logger.info("Room message broadcast completed in " + roomName);
            }
        } else {
            logger.warn("Room message failed - room not found: " + roomName);
        }
    }


    @Override
    public String[] getOnlineUsers() throws RemoteException {
        return connectedClients.keySet().toArray(new String[0]);
    }

    @Override
    public String[] getAvailableRooms() throws RemoteException {
        return chatRooms.keySet().toArray(new String[0]);
    }

    private void notifyUserStatus(String username, boolean online) {
        logger.info("Notifying user status change: " + username + " - " + (online ? "online" : "offline"));
        connectedClients.forEach((user, callback) -> {
            if (!user.equals(username)) {
                try {
                    callback.onUserStatusChanged(username, online);
                } catch (RemoteException e) {
                    connectedClients.remove(user);
                    logger.error("Failed to notify user " + user + " about status change: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public String[] getRoomClients(String roomName) throws RemoteException {
        if (chatRooms.containsKey(roomName)) {
            ChatRoom room = chatRooms.get(roomName);
            return room.getClients().stream().map(User::getUsername).toArray(String[]::new);
        }
        return new String[0];
    }

    @Override
    public void sendFile(String sender, String recipient, byte[] fileData, String fileName) throws RemoteException {
        logger.info("File transfer attempt from " + sender + " to " + recipient + ": " + fileName);
        User senderUser = dbHelper.getUser(sender);
        User recipientUser = dbHelper.getUser(recipient);

        if (senderUser != null && recipientUser != null) {
            Message fileMessage = new Message(sender, recipient, "FILE:" + fileName, System.currentTimeMillis());
            fileMessage.setFileData(fileData);
            dbHelper.saveMessage(fileMessage);

            ClientCallback recipientCallback = connectedClients.get(recipient);
            if (recipientCallback != null) {
                recipientCallback.onFileReceived(fileMessage);
                logger.info("File delivered successfully from " + sender + " to " + recipient + ": " + fileName);
            } else {
                logger.warn("Recipient offline, file stored: " + recipient);
            }
        } else {
            logger.error("File sending failed - invalid users: " + sender + " -> " + recipient);
        }
    }

    @Override
    public void sendFileToRoom(String sender, String roomName, byte[] fileData, String fileName) throws RemoteException {
        logger.info("Room file transfer attempt from " + sender + " to room: " + roomName);
        if (chatRooms.containsKey(roomName)) {
            ChatRoom room = chatRooms.get(roomName);
            User senderUser = userProfiles.get(sender);

            if (senderUser != null) {
                Message fileMessage = new Message(sender, roomName, "FILE:" + fileName, System.currentTimeMillis());
                fileMessage.setFileData(fileData);
                fileMessage.setRoomMessage(true);
                dbHelper.saveRoomMessage(fileMessage, roomName);

                for (User client : room.getClients()) {
                    ClientCallback callback = connectedClients.get(client.getUsername());
                    if (callback != null && !client.getUsername().equals(sender)) {
                        callback.onRoomFileReceived(roomName, fileMessage);
                        logger.info("File delivered to room member: " + client.getUsername());
                    }
                }
                logger.info("Room file broadcast completed in " + roomName + ": " + fileName);
            }
        } else {
            logger.warn("Room file transfer failed - room not found: " + roomName);
        }
    }

}