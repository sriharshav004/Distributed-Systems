public class BFSTree {
    private int receivedMessagesCount;
    private Node currNode;

    public BFSTree(Node currNode) {
        this.currNode = currNode;
    }

    public void buildTree() {
        System.out.println("BFS_BUILD -> Starting BFS build");

        this.currNode.startBFSBuild();
        this.currNode.setVisited(true);

        if (this.currNode.isNodeLeader()) {
            this.sendSearch();
        }

        this.receivedMessagesCount = 0;
        while (true) {
            this.listenMessages();

            if (this.receivedMessagesCount == this.currNode.getNeighbours().size())
                break;
        }

        System.out.println("BFS_BUILD -> Completed. Child Nodes: " + this.currNode.getChildNodes() + "Degree: "
                + this.currNode.getDegree());
    }

    private void sendSearch() {
        for (TCPClient client : this.currNode.getNeighbourClients()) {
            System.out.println("BFS_BUILD -> Sending message to server: " + client.getServerNode().getUID());

            new Message(this.currNode.getUID(), this.currNode.getUID(), Message.MessageType.BFS_BUILD_CHILD_REQUEST)
                    .send(client.getServerNode(), client.getOutputStream());
        }
    }

    private void listenMessages() {
        Message latestMessage = this.currNode.popLatestReceivedMessage();

        switch (latestMessage.getType()) {
            case BFS_BUILD_CHILD_REQUEST:
                System.out.println("BFS_BUILD -> Receved child request from " + latestMessage.getSenderUID());
                if (this.currNode.isNodeVisited()) {
                    this.sendChildRequestRejected(latestMessage.getSenderUID());
                } else {
                    this.currNode.setParent(latestMessage.getSenderUID());
                    this.currNode.setVisited(true);

                    this.sendSearch();
                }
                break;

            case BFS_BUILD_CHILD_REQUEST_ACCEPTED:
                System.out.println("BFS_BUILD -> Receved child request accepted from " + latestMessage.getSenderUID());
                this.currNode.addChildNode(latestMessage.getSenderUID());

                this.receivedMessagesCount = this.receivedMessagesCount + 1;
                if (this.receivedMessagesCount == this.currNode.getNeighbours().size()) {
                    this.sendChildRequestAccepted(this.currNode.getParentUID());
                }
                break;

            case BFS_BUILD_CHILD_REQUEST_REJECTED:
                System.out.println("BFS_BUILD -> Receved child request rejected from " + latestMessage.getSenderUID());
                this.receivedMessagesCount = this.receivedMessagesCount + 1;
                if (this.receivedMessagesCount == this.currNode.getNeighbours().size()) {
                    this.sendChildRequestAccepted(this.currNode.getParentUID());
                }
                break;

            default:
                return;
        }
    }

    private void sendChildRequestAccepted(int clientUID) {
        TCPClient client = this.currNode.getClientConnection(clientUID);

        new Message(this.currNode.getUID(), this.currNode.getParentUID(),
                Message.MessageType.BFS_BUILD_CHILD_REQUEST_ACCEPTED)
                .send(client.getServerNode(), client.getOutputStream());
    }

    private void sendChildRequestRejected(int clientUID) {
        TCPClient client = this.currNode.getClientConnection(clientUID);

        new Message(this.currNode.getUID(), this.currNode.getParentUID(),
                Message.MessageType.BFS_BUILD_CHILD_REQUEST_REJECTED)
                .send(client.getServerNode(), client.getOutputStream());
    }
}
