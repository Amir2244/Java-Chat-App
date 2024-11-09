import java.io.Serializable;

public class Message implements Serializable {
    private final String sender;
    private final String recipient;
    private final String content;
    private final long timestamp;
    private boolean isRoomMessage;
    private byte[] fileData;

    public Message(String sender, String recipient, String content, long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = timestamp;
        this.isRoomMessage = false;
        this.fileData = null;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRoomMessage() {
        return isRoomMessage;
    }

    public void setRoomMessage(boolean roomMessage) {
        isRoomMessage = roomMessage;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public boolean isFile() {
        return content != null && content.startsWith("FILE:");
    }

    public String getFileName() {
        return isFile() ? content.substring(5) : null;
    }
}
