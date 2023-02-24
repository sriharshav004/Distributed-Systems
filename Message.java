public class Message {
    public enum MessageType {
        HANDSHAKE
    }

    private String text;
    private MessageType type;

    public Message(String text, MessageType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return this.text;
    }
}
