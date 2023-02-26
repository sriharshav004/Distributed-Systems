import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        BFS_ADD_CHILD_REQUEST,
        BFS_ADD_CHILD_REQUEST_ACCEPTED,
        BFS_ADD_CHILD_REQUEST_REJECTED,
        BFS_BEGIN_CHILD_SEARCH,
        BFS_CHILD_BUILD_COMPLETED,
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

    public Message(int senderUID, MessageType type) {
        this.senderUID = senderUID;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
