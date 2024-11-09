import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ChatServer {
    private static final Logger logger = Logger.getInstance();
    private static final int PORT = 1099;

    public static void main(String[] args) {
        try {
            logger.info("Initializing Chat Server...");


            System.setProperty("java.security.policy", "SecurityPolicy.policy");
            System.setProperty("java.rmi.server.hostname", "localhost");
            logger.info("System properties set successfully");


            ChatService chatService = new ChatServiceImpl();
            logger.info("Chat service implementation initialized");


            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind("ChatService", chatService);
            logger.info("RMI Registry created and bound on port " + PORT);

            logger.info("Chat server started successfully and ready for connections");

        } catch (Exception e) {
            logger.error("Server initialization failed: " + e.getMessage());
        }
    }
}