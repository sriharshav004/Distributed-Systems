import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node implements Serializable {
    private ArrayList<Node> allNodes;
    private String hostName;
    private ArrayList<Integer> neighbours;
    private List<TCPClient> neighbourClients = Collections.synchronizedList(new ArrayList<TCPClient>());
    private int port;
    private int UID;

    // variables for leader election algorithm
    private Boolean isLeaderElectionCompleted;
    private int leaderUID;
    private List<Message> receivedMessages = Collections.synchronizedList(new ArrayList<Message>());

    public Node() {
    }

    public Node(int UID, String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.UID = UID;

        this.neighbours = new ArrayList<Integer>();
    }

    public void addLeaderElectionMessage(Message msg) {
        synchronized (this.receivedMessages) {
            this.receivedMessages.add(msg);
        }
    }

    public void addNeighbour(int neighbourUID) {
        this.neighbours.add(neighbourUID);
    }

    public void addNeighbourClient(TCPClient client) {
        synchronized (this.neighbourClients) {
            this.neighbourClients.add(client);
        }
    }

    public Boolean areAllNeighboursOnline() {
        return this.neighbours.size() == this.neighbourClients.size();
    }

    public void endLeaderElection(int leaderUID) {
        this.isLeaderElectionCompleted = true;
        this.leaderUID = leaderUID;
    }

    public ArrayList<Node> getAllNodes() {
        return this.allNodes;
    }

    public String getHostName() {
        return this.hostName;
    }

    public List<TCPClient> getNeighbourClients() {
        return this.neighbourClients;
    }

    public ArrayList<Integer> getNeighbours() {
        return this.neighbours;
    }

    public int getPort() {
        return this.port;
    }

    public List<Message> getReceivedMessages() {
        return this.receivedMessages;
    }

    public int getUID() {
        return this.UID;
    }

    public boolean isNodeNeighbour(int UID) {
        for (int neighbour : this.neighbours) {
            if (UID == neighbour)
                return true;
        }
        return false;
    }

    public void setAllNodes(ArrayList<Node> allNodes) {
        this.allNodes = allNodes;
    }

    public void startLeaderElection() {
        this.isLeaderElectionCompleted = false;
        this.leaderUID = -1;
    }
}
