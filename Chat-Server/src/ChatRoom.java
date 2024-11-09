import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom implements Serializable {
    private final String name;
    private final List<User> clients;
    private final List<User> blockedClients;
    private final transient Logger logger = Logger.getInstance();
    private static final String USER_LABEL = "User ";
    public ChatRoom(String name) {
        this.name = name;
        this.clients = new ArrayList<>();
        blockedClients = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public void addClient(User client) {
        if (!clients.contains(client)) {
            clients.add(client);
            logger.info(USER_LABEL + client.getUsername() + " joined room: " + name);
        }
    }

    public void removeClient(User client) {
        clients.remove(client);
        logger.info(USER_LABEL + client.getUsername() + " left room: " + name);
    }

    public void blockClient(User client) {
        blockedClients.add(client);
        clients.remove(client);
        logger.warn(USER_LABEL + client.getUsername() + " blocked in room: " + name);
    }

    public void broadcastMessage(User sender, String message) {
        logger.info("Broadcasting message in room " + name + " from user: " + sender.getUsername() + ": " + message);

        for (User client : clients) {
            if (!blockedClients.contains(client) && !client.equals(sender)) {
                logger.info("Message delivered to " + client.getUsername() + " in room: " + name);
            } else {
                logger.info("Message blocked for user " + client.getUsername() + " in room: " + name);
            }
        }
    }

    public boolean isUserInRoom(User user) {
        return clients.contains(user);
    }

    public List<User> getClients() {
        return new ArrayList<>(clients);
    }

}