import java.rmi.RemoteException;

public class SendMessageCommand implements Command {
    private final ChatService chatService;
    private final String sender;
    private final String recipient;
    private final String message;

    public SendMessageCommand(ChatService chatService, String sender, String recipient, String message) {
        this.chatService = chatService;
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    @Override
    public void execute() throws RemoteException {
        chatService.sendMessage(sender, recipient, message);
    }
}
