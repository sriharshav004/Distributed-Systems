import java.util.HashMap;
import java.util.Map;

public class BFSTree {
    private Node currNode;
    private boolean isSendSearchCompleted = false;
    private int receivedMessagesCount;
    private HashMap<Integer, Message.MessageType> neighboursLastComm;

    public BFSTree(Node currNode) {
        this.currNode = currNode;
    }

    public void buildTree() {
        System.out.println("BFS_BUILD -> Starting BFS build");

        this.currNode.startBFSBuild();
        if (this.currNode.isNodeLeader()) {
            this.sendSearch();
        }

        this.receivedMessagesCount = 0;
        while (true) {
            this.listenMessages();

            if (this.isTreeConstructionCompleted())
                break;
        }

        System.out.println("BFS_BUILD -> Completed. Child Nodes: " + this.currNode.getChildNodes() + "Degree: "
                + this.currNode.getDegree());
    }

    private void sendSearch() {
        for (TCPClient client : this.currNode.getNeighbourClients()) {
            if (client.getServerNode().getUID() != this.currNode.getParentUID()) {
                System.out.println("BFS_BUILD -> Sending message to server: " + client.getServerNode().getUID());

                new Message(this.currNode.getUID(), this.currNode.getUID(), Message.MessageType.BFS_BUILD_CHILD_REQUEST)
                        .send(client.getServerNode(), client.getOutputStream());
            }
        }
        this.isSendSearchCompleted = true;
    }

    private void listenMessages() {
        Message latestMessage = this.currNode.popLatestReceivedMessage();

        if (latestMessage.getSenderUID() == -1)
            return;

        boolean didAllNeighboursReply = false;
        neighboursLastComm.put(latestMessage.getSenderUID(), latestMessage.getType());

        switch (latestMessage.getType()) {
            case BFS_BUILD_CHILD_REQUEST:
                System.out.println("BFS_BUILD -> BFS_BUILD_CHILD_REQUEST from " + latestMessage.getSenderUID());
                if (this.currNode.isNodeVisited()) {
                    this.sendChildRequestRejected(latestMessage.getSenderUID());
                } else {
                    this.currNode.setParent(latestMessage.getSenderUID());
                    this.currNode.setVisited(true);

                    this.sendChildRequestAccepted();
                }
                break;

            case BFS_BUILD_CHILD_REQUEST_ACCEPTED:
                System.out.println(
                        "BFS_BUILD -> Received BFS_BUILD_CHILD_REQUEST_ACCEPTED from " + latestMessage.getSenderUID());
                this.currNode.addChildNode(latestMessage.getSenderUID());

                this.receivedMessagesCount = this.receivedMessagesCount + 1;
                didAllNeighboursReply = this.didAllNeighboursReply();

                if (didAllNeighboursReply && (this.currNode.isNodeLeader() || !this.isSendSearchCompleted)) {
                    this.sendBeginChildSearch();
                } else if (didAllNeighboursReply) {
                    this.sendChildSearchComplete();
                }

                break;

            case BFS_BUILD_CHILD_REQUEST_REJECTED:
                System.out
                        .println("BFS_BUILD -> BFS_BUILD_CHILD_REQUEST_REJECTED from " + latestMessage.getSenderUID());

                this.receivedMessagesCount = this.receivedMessagesCount + 1;
                didAllNeighboursReply = this.didAllNeighboursReply();

                if (this.didAllNeighboursReply() && (this.currNode.isNodeLeader() || !this.isSendSearchCompleted)) {
                    this.sendBeginChildSearch();
                } else if (didAllNeighboursReply) {
                    this.sendChildSearchComplete();
                }
                break;

            case BFS_BUILD_BEGIN_CHILD_SEARCH:
                System.out.println("BFS_BUILD -> BFS_BUILD_BEGIN_CHILD_SEARCH from " + latestMessage.getSenderUID());
                this.sendSearch();
                break;

            case BFS_BUILD_CHILD_SEARCH_COMPLETED:
            default:
                return;
        }
    }

    private boolean isTreeConstructionCompleted() {
        for (Map.Entry<Integer, Message.MessageType> entry : neighboursLastComm.entrySet()) {
            int neighbourUID = entry.getKey();
            Message.MessageType messageType = entry.getValue();

            if (neighbourUID == this.currNode.getParentUID()) {
                if (messageType != Message.MessageType.BFS_BUILD_BEGIN_CHILD_SEARCH)
                    return false;
            } else if (this.currNode.isNodeChild(neighbourUID)) {
                if (messageType != Message.MessageType.BFS_BUILD_CHILD_SEARCH_COMPLETED)
                    return false;
            } else {
                if (messageType != Message.MessageType.BFS_BUILD_CHILD_REQUEST_REJECTED)
                    return false;
            }
        }
        return true;
    }

    private boolean didAllNeighboursReply() {
        return this.currNode.isNodeLeader()
                ? this.receivedMessagesCount == this.currNode.getNeighbours().size()
                : this.receivedMessagesCount + 1 == this.currNode.getNeighbours().size();
    }

    private void sendChildRequestAccepted() {
        TCPClient client = this.currNode.getClientConnection(this.currNode.getParentUID());

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

    private void sendBeginChildSearch() {
        for (int childUID : this.currNode.getChildNodes()) {
            TCPClient client = this.currNode.getClientConnection(childUID);

            new Message(this.currNode.getUID(), this.currNode.getUID(),
                    Message.MessageType.BFS_BUILD_BEGIN_CHILD_SEARCH)
                    .send(client.getServerNode(), client.getOutputStream());
        }
    }

    private void sendChildSearchComplete() {
        TCPClient client = this.currNode.getClientConnection(this.currNode.getParentUID());

        new Message(this.currNode.getUID(), this.currNode.getParentUID(),
                Message.MessageType.BFS_BUILD_CHILD_SEARCH_COMPLETED)
                .send(client.getServerNode(), client.getOutputStream());
    }
}
