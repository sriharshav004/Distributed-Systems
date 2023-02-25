public class PelegsLeaderElection {
    private TCPServer server;
    private Node currNode;

    private int phase;

    public PelegsLeaderElection(TCPServer server) {
        this.server = server;
        this.currNode = this.server.getServerNode();
    }

    public void startElection() {
        System.out.println("Starting leader election on node:" + this.currNode.getUID());

        this.currNode.startLeaderElection();
        this.phase = 0;

        this.sendMessages();
        this.listenMessages();
    }

    public void sendMessages() {
        for (TCPClient client : this.currNode.getNeighbourClients()) {
            Node cliNode = client.getClientNode();
            System.out.println("Sending message to client: " + cliNode.getUID());

            new Message(currNode, currNode.getMaxDistance(), currNode.getMaxUID(), this.phase)
                    .send(cliNode, client.getOutputStream());
        }
    }

    public void listenMessages() {
        while (!this.didAllNeighboursReply()) {
            try {
                System.out.println("Waiting to receive replies from all neighbours.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Received all replies. Proceeding to next phase.");
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
}
