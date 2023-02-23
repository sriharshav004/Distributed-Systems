import java.util.ArrayList;

public class Node {
    private String hostName;
    private int listeningPort;
    private ArrayList<Integer> neighbours;
    private int UID;

    public Node() {
    }

    public Node(int UID, String hostName, int listeningPort) {
        this.UID = UID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;

        neighbours = new ArrayList<Integer>();
    }

    public void addNeighbour(int neighbourUID) {
        this.neighbours.add(neighbourUID);
    }

    public String getHostName() {
        return this.hostName;
    }

    public int getListeningPort() {
        return this.listeningPort;
    }

    public ArrayList<Integer> getNeighbours() {
        return this.neighbours;
    }

    public int getUID() {
        return this.UID;
    }
}
