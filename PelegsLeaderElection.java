import java.util.stream.Stream;

public class PelegsLeaderElection {
    private TCPServer server;
    private Node currNode;

    private int exitCount;
    private int maxDistance;
    private int maxUID;
    private int phase;

    public PelegsLeaderElection(TCPServer server) {
        this.server = server;
        this.currNode = this.server.getServerNode();
    }

    public void startElection() {
        System.out.println("LEADER_ELEC -> Starting leader election on node: " + this.currNode.getUID());

        this.currNode.startLeaderElection();
        this.exitCount = 0;
        this.maxDistance = 0;
        this.maxUID = this.currNode.getUID();
        this.phase = 0;

        while (true) {
            this.sendMessages();
            this.listenMessages();

            if (this.exitCount == 2) {
                System.out.println(
                        "LEADER_ELEC -> Leader election complete. UID: " + this.currNode.getUID() + " is the leader");
                break;
            }
        }
    }

    public void sendMessages() {
        for (TCPClient client : this.currNode.getNeighbourClients()) {
            System.out.println("LEADER_ELEC -> Sending message to server: " + client.getServerNode().getUID());

            new Message(currNode, this.maxDistance, this.maxUID, this.phase)
                    .send(client.getServerNode(), client.getOutputStream());
        }
    }

    public void listenMessages() {
        while (!this.didAllNeighboursReply()) {
            try {
                System.out.println("LEADER_ELEC -> Waiting to receive replies from all neighbours");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("LEADER_ELEC -> Received all replies. Proceeding to next phase");

        int newMaxUID = this.getMaxUID();

        if (newMaxUID > this.maxUID) {
            this.exitCount = 0;
            this.maxDistance = this.phase + 1;
            this.maxUID = newMaxUID;
        } else if (newMaxUID == this.maxUID) {
            int newMaxDistance = this.getMaxDistance();

            if (newMaxDistance > this.maxDistance) {
                this.exitCount = 0;
                this.maxDistance = newMaxDistance;
            } else if (newMaxDistance == this.maxDistance) {
                this.exitCount = this.exitCount + 1;
            }
        } else if (newMaxUID < this.maxUID) {
            this.exitCount = 1;
        }

        this.phase = this.phase + 1;
    }

    public Boolean didAllNeighboursReply() {
        long receivedCount = this.currNode
                .getReceivedMessages()
                .stream()
                .filter(msg -> msg.getPhase() == this.phase)
                .count();

        int neighboursCount = this.currNode.getNeighbours().size();
        return neighboursCount == (int) receivedCount;
    }

    public int getMaxUID() {
        Stream<Message> currPhaseMessages = this.currNode
                .getReceivedMessages()
                .stream()
                .filter(msg -> msg.getPhase() == this.phase);

        int newMaxUID = currPhaseMessages.toList().get(0).getMaxUID();
        for (Message msg : currPhaseMessages.toList()) {
            if (newMaxUID < msg.getMaxUID())
                newMaxUID = msg.getMaxUID();
        }
        return newMaxUID;
    }

    public int getMaxDistance() {
        Stream<Message> currPhaseMessages = this.currNode
                .getReceivedMessages()
                .stream()
                .filter(msg -> msg.getPhase() == this.phase && msg.getMaxUID() == this.maxUID);

        int newMaxDistance = currPhaseMessages.toList().get(0).getMaxDistance();
        for (Message msg : currPhaseMessages.toList()) {
            if (newMaxDistance < msg.getMaxDistance())
                newMaxDistance = msg.getMaxDistance();
        }
        return newMaxDistance;
    }
}
