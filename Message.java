import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        HANDSHAKE
    }

    private Node clientNode;
    private String text;
    private MessageType type;

    public Message(Node clientNode, String text, MessageType type) {
        this.clientNode = clientNode;
        this.text = text;
        this.type = type;
    }

    public Node getClientNode() {
        return this.clientNode;
    }

    public String getText() {
        return this.text;
    }

    public MessageType getType() {
        return this.type;
    }

    public void send(Node serverNode, ObjectOutputStream outToServer) {
        try {
            outToServer.writeObject(this);
            outToServer.flush();

            System.out.println("Sent message: " + this.getText() + " to server UID: " + serverNode.getUID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
