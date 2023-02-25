import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        HANDSHAKE,
        LEADER_ELECTION_IN_PROGRESS,
        LEADER_ELECTION_COMPLETE
    }

    private int senderUID;
    private MessageType type;

    private String text;

    private int maxDistance;
    private int maxUID = -1;
    private int phase;

    public Message() {
    }

    public Message(int senderUID, int maxDistance, int maxUID, int phase, MessageType type) {
        this.senderUID = senderUID;
        this.maxDistance = maxDistance;
        this.maxUID = maxUID;
        this.phase = phase;
        this.type = type;
    }

    public Message(int senderUID, String text) {
        this.senderUID = senderUID;
        this.text = text;
        this.type = MessageType.HANDSHAKE;
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

    public int getSenderUID() {
        return this.senderUID;
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
