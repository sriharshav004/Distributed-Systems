import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        BFS_BUILD_CHILD_REQUEST,
        BFS_BUILD_CHILD_REQUEST_ACCEPTED,
        BFS_BUILD_CHILD_REQUEST_REJECTED,
        HANDSHAKE,
        LEADER_ELECTION_IN_PROGRESS,
        LEADER_ELECTION_COMPLETE
    }

    private int senderUID = -1;
    private MessageType type;

    // variables for handshake message
    private String text;

    // variables for leader election algorithm
    private int maxDistance;
    private int maxUID = -1;
    private int phase;

    // variables for building BFS tree
    private int parentUID = -1;

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

    public Message(int senderUID, int parentUID, MessageType type) {
        this.senderUID = senderUID;
        this.parentUID = parentUID;
        this.type = type;
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

            // System.out.println("Sent message: " + this.getType() + " to server UID: " +
            // serverNode.getUID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
