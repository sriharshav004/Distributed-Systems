import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        HANDSHAKE,
        LEADER_ELECTION_IN_PROGRESS
    }

    private Node cliNode;
    private MessageType type;

    private String text;

    private int maxDistance;
    private int maxUID;
    private int phase;

    public Message(Node cliNode, int maxDistance, int maxUID, int phase) {
        this.cliNode = cliNode;
        this.maxDistance = maxDistance;
        this.maxUID = maxUID;
        this.phase = phase;
        this.type = MessageType.LEADER_ELECTION_IN_PROGRESS;
    }

    public Message(Node cliNode, String text) {
        this.cliNode = cliNode;
        this.text = text;
        this.type = MessageType.HANDSHAKE;
    }

    public Node getClientNode() {
        return this.cliNode;
    }

    public int getMaxDistance() {
        return this.maxDistance;
    }

    public int getMaxUID() {
        return this.maxUID;
    }

    public int getPhase() {
        return this.phase;
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

            System.out.println("Sent message: " + this.getType() + " to server UID: " + serverNode.getUID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
