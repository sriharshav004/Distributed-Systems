import java.util.List;
import java.util.stream.Collectors;

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
                        "LEADER_ELEC -> Leader election complete. UID: " + this.maxUID + " is the leader");

                this.electLeader();
                break;
            }
        }
    }

    private void sendMessages() {
        for (TCPClient client : this.currNode.getNeighbourClients()) {
            System.out.println("LEADER_ELEC -> Sending message to server: " + client.getServerNode().getUID());

            new Message(this.currNode.getUID(), this.maxDistance, this.maxUID, this.phase,
                    Message.MessageType.LEADER_ELECTION_IN_PROGRESS)
                    .send(client.getServerNode(), client.getOutputStream());
        }
    }

    private void listenMessages() {
        while (!this.didAllNeighboursReply()) {
            try {
                System.out.println("LEADER_ELEC -> Waiting to receive replies from all neighbours");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("LEADER_ELEC -> Received all replies. Proceeding to next phase");
        Message electionCompleteMessage = this.getElectionCompleteMessage();

        if (electionCompleteMessage.getMaxUID() != -1) {
            this.exitCount = 2;
            this.maxDistance = electionCompleteMessage.getMaxDistance();
            this.maxUID = electionCompleteMessage.getMaxUID();
            this.phase = this.phase + 1;

            return;
        }

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

    private boolean didAllNeighboursReply() {
        long receivedCount = this.currNode
                .getReceivedMessages()
                .stream()
                .filter(msg -> msg.getPhase() == this.phase)
                .count();

        int neighboursCount = this.currNode.getNeighbours().size();
        return neighboursCount == (int) receivedCount;
    }

    private Message getElectionCompleteMessage() {
        List<Message> currPhaseMessages = this.currNode
                .getReceivedMessages()
                .stream()
                .filter(msg -> msg.getPhase() == this.phase
                        && msg.getType() == Message.MessageType.LEADER_ELECTION_COMPLETE)
                .collect(Collectors.toList());

        if (currPhaseMessages.size() > 0) {
            return currPhaseMessages.get(0);
        }
        return new Message();
    }

    private int getMaxUID() {
        List<Message> currPhaseMessages = this.currNode
                .getReceivedMessages()
                .stream()
                .filter(msg -> msg.getPhase() == this.phase)
                .collect(Collectors.toList());

        int newMaxUID = currPhaseMessages.get(0).getMaxUID();
        for (Message msg : currPhaseMessages) {
            if (newMaxUID < msg.getMaxUID())
                newMaxUID = msg.getMaxUID();
        }
        return newMaxUID;
    }

    private int getMaxDistance() {
        List<Message> currPhaseMessages = this.currNode
                .getReceivedMessages()
                .stream()
                .filter(msg -> msg.getPhase() == this.phase && msg.getMaxUID() == this.maxUID)
                .collect(Collectors.toList());

        int newMaxDistance = currPhaseMessages.get(0).getMaxDistance();
        for (Message msg : currPhaseMessages) {
            if (newMaxDistance < msg.getMaxDistance())
                newMaxDistance = msg.getMaxDistance();
        }
        return newMaxDistance;
    }

    private void electLeader() {
        this.currNode.endLeaderElection(this.maxUID);

        for (TCPClient client : this.currNode.getNeighbourClients()) {
            System.out.println("LEADER_ELEC -> Sending complete message to server: " + client.getServerNode().getUID());

            new Message(currNode.getUID(), this.maxDistance, this.maxUID, this.phase,
                    Message.MessageType.LEADER_ELECTION_COMPLETE)
                    .send(client.getServerNode(), client.getOutputStream());
        }
    }
}
