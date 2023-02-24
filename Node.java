import java.util.ArrayList;

public class Node {
    private ArrayList<Node> allNodes;
    private String hostName;
    private ArrayList<Integer> neighbours;
    private int port;
    private int UID;

    public Node() {
    }

    public Node(int UID, String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.UID = UID;

        this.neighbours = new ArrayList<Integer>();
    }

    public void addNeighbour(int neighbourUID) {
        this.neighbours.add(neighbourUID);
    }

    public ArrayList<Node> getAllNodes() {
        return this.allNodes;
    }

    public String getHostName() {
        return this.hostName;
    }

    public ArrayList<Integer> getNeighbours() {
        return this.neighbours;
    }

    public int getPort() {
        return this.port;
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
}
