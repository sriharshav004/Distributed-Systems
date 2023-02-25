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
    private List<Message> receivedMessages = Collections.synchronizedList(new ArrayList<Message>());

    // variables for leader election algorithm
    private boolean isLeaderElectionCompleted;
    private int leaderUID;

    // variables for building BFS tree
    private List<Integer> childNodes;
    private int parentUID;
    private boolean visited;

    public Node() {
    }

    public Node(int UID, String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.UID = UID;

        this.neighbours = new ArrayList<Integer>();
    }

    public void addChildNode(int childUID) {
        this.childNodes.add(childUID);
    }

    public void addNeighbour(int neighbourUID) {
        this.neighbours.add(neighbourUID);
    }

    public void addNeighbourClient(TCPClient client) {
        synchronized (this.neighbourClients) {
            this.neighbourClients.add(client);
        }
    }

    public void addReceivedMessage(Message msg) {
        synchronized (this.receivedMessages) {
            this.receivedMessages.add(msg);
        }
    }

    public boolean areAllNeighboursOnline() {
        return this.neighbours.size() == this.neighbourClients.size();
    }

    public void endLeaderElection(int leaderUID) {
        this.isLeaderElectionCompleted = true;
        this.leaderUID = leaderUID;
        this.receivedMessages.clear();
    }

    public ArrayList<Node> getAllNodes() {
        return this.allNodes;
    }

    public List<Integer> getChildNodes() {
        return this.childNodes;
    }

    public TCPClient getClientConnection(int clientUID) {
        for (TCPClient client : neighbourClients) {
            if (clientUID == client.getServerNode().getUID())
                return client;
        }
        return new TCPClient();
    }

    public int getDegree() {
        return this.isNodeLeader() ? this.childNodes.size() : this.childNodes.size() + 1;
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

    public int getParentUID() {
        return this.parentUID;
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

    public boolean isNodeLeader() {
        return this.UID == this.leaderUID;
    }

    public boolean isNodeNeighbour(int UID) {
        for (int neighbour : this.neighbours) {
            if (UID == neighbour)
                return true;
        }
        return false;
    }

    public boolean isNodeVisited() {
        return this.visited;
    }

    public Message popLatestReceivedMessage() {
        synchronized (this.receivedMessages) {
            return this.receivedMessages.remove(0);
        }
    }

    public void setAllNodes(ArrayList<Node> allNodes) {
        this.allNodes = allNodes;
    }

    public void setParent(int parentUID) {
        this.parentUID = parentUID;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void startBFSBuild() {
        this.childNodes.clear();
        this.parentUID = -1;
        this.visited = false;
        this.receivedMessages.clear();
    }

    public void startLeaderElection() {
        this.isLeaderElectionCompleted = false;
        this.leaderUID = -1;
    }
}
