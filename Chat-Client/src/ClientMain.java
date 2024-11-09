import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ClientMain {
    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());

    public static void main(String[] args) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Starting chat client application");
        }
        ChatClient client = new ChatClient(HOST, PORT);
        Scanner scanner = new Scanner(System.in);

        while (client.connectivityCheck()) {
            displayMenu();
            int choice = getUserChoice(scanner);
            processUserChoice(choice, scanner, client);
        }
    }

    private static void displayMenu() {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "\n=== Chat Client Menu ===");
            LOGGER.log(Level.INFO, "1. Sign In");
            LOGGER.log(Level.INFO, "2. Sign Up");
            LOGGER.log(Level.INFO, "3. Send Message");
            LOGGER.log(Level.INFO, "4. Create Chat Room");
            LOGGER.log(Level.INFO, "5. Delete Chat Room");
            LOGGER.log(Level.INFO, "6. Exit");
            LOGGER.log(Level.INFO, "Choose an option: ");
        }
    }

    private static int getUserChoice(Scanner scanner) {
        int choice = scanner.nextInt();
        scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "User selected option: {0}", choice);
        }
        return choice;
    }

    private static void processUserChoice(int choice, Scanner scanner, ChatClient client) {
        try {
            switch (choice) {
                case 1:
                    handleSignIn(scanner, client);
                    break;
                case 2:
                    handleSignUp(scanner, client);
                    break;
                case 3:
                    handleSendMessage(scanner, client);
                    break;
                case 4:
                    handleCreateChatRoom(scanner, client);
                    break;
                case 5:
                    handleDeleteChatRoom(scanner, client);
                    break;
                case 6:
                    handleExit(scanner);
                    break;
                default:
                    handleInvalidOption(choice);
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    private static void handleSignIn(Scanner scanner, ChatClient client) throws RemoteException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Username: ");
        }
        String username = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Password: ");
        }
        String password = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Attempting sign in for user: {0}", username);
        }
        client.signIn(username, password);
    }

    private static void handleSignUp(Scanner scanner, ChatClient client) throws RemoteException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Username: ");
        }
        String newUsername = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Password: ");
        }
        String newPassword = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "First Name: ");
        }
        String firstName = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Last Name: ");
        }
        String lastName = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Attempting to create new user: {0}", newUsername);
        }
        client.signUp(newUsername, newPassword, firstName, lastName);
    }

    private static void handleSendMessage(Scanner scanner, ChatClient client) throws RemoteException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Your username: ");
        }
        String sender = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Recipient: ");
        }
        String recipient = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Message: ");
        }
        String message = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Sending message from {0} to {1}", new Object[]{sender, recipient});
        }
        client.sendMessage(sender, recipient, message);
    }
    private static void handleCreateChatRoom(Scanner scanner, ChatClient client) throws RemoteException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Room name: ");
        }
        String roomName = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Your username: ");
        }
        String owner = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Creating new chat room: {0} with owner: {1}", new Object[]{roomName, owner});
        }
        client.addChatRoom(roomName);
    }

    private static void handleDeleteChatRoom(Scanner scanner, ChatClient client) throws RemoteException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Room name to delete: ");
        }
        String roomToDelete = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Your username: ");
        }
        String owner = scanner.nextLine();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Deleting chat room: {0} by owner: {1}", new Object[]{roomToDelete, owner});
        }
        client.deleteRoom(roomToDelete, owner);
    }

    private static void handleExit(Scanner scanner) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "User requested to exit application");
            LOGGER.log(Level.INFO, "Goodbye!");
        }
        scanner.close();
        System.exit(0);
    }

    private static void handleInvalidOption(int choice) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.log(Level.WARNING, "Invalid option selected: {0}", choice);
            LOGGER.log(Level.WARNING, "Invalid option. Please try again.");
        }
    }

    private static void handleError(Exception e) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.log(Level.SEVERE, "Error occurred: {0}", e.getMessage());
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        }
    }
}