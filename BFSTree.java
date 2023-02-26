import java.util.ArrayList;
import java.util.List;

public class BFSTree {
    private Node currNode;
    private int receivedAckMessagesCount;
    private int receivedCompletedMessagesCount;
    private List<Integer> rejectedNodes = new ArrayList<Integer>();
    // private HashMap<Integer, Message.MessageType> neighboursLastComm;

    public BFSTree(Node currNode) {
        this.currNode = currNode;
        // this.initLastCommWithNeighbours();
    }

    // private void initLastCommWithNeighbours() {
    // neighboursLastComm = new HashMap<>();

    // for (int neighbourUID : this.currNode.getNeighbours()) {
    // neighboursLastComm.put(neighbourUID, Message.MessageType.HANDSHAKE);
    // }
    // }

    public void buildTree() {
        System.out.println("BFS_BUILD -> Starting BFS build");

        this.currNode.startBFSBuild();
        if (this.currNode.isNodeLeader()) {
            this.sendSearch();
        }

        this.receivedAckMessagesCount = 0;
        this.receivedCompletedMessagesCount = 0;

        while (true) {
            this.listenMessages();

            if (this.isTreeConstructionCompleted())
                break;
        }

        System.out.println("BFS_BUILD -> Completed for UID: " + this.currNode.getUID() + ". Child Nodes: "
                + this.currNode.getChildNodes() + " Degree: "
                + this.currNode.getDegree());
    }

    private void sendSearch() {
        int sentMessagesCount = 0;
        for (TCPClient client : this.currNode.getNeighbourClients()) {
            int clientUID = client.getServerNode().getUID();

            if (clientUID != this.currNode.getParentUID() && this.rejectedNodes.indexOf(clientUID) == -1) {
                System.out.println("BFS_BUILD -> Sending message to server: " + client.getServerNode().getUID());

                new Message(this.currNode.getUID(), this.currNode.getUID(), Message.MessageType.BFS_BUILD_CHILD_REQUEST)
                        .send(client.getServerNode(), client.getOutputStream());

                sentMessagesCount++;
            }
        }

        if (sentMessagesCount == 0) {
            this.sendChildSearchComplete();
        }
    }

    private void listenMessages() {
        Message latestMessage = this.currNode.popLatestReceivedMessage();

        if (latestMessage.getSenderUID() == -1)
            return;

        // neighboursLastComm.put(latestMessage.getSenderUID(),
        // latestMessage.getType());

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

                this.receivedAckMessagesCount = this.receivedAckMessagesCount + 1;

                if (this.didAllNeighboursReplyAck()) {
                    this.sendBeginChildSearch();
                    this.receivedAckMessagesCount = 0;
                }

                break;

            case BFS_BUILD_CHILD_REQUEST_REJECTED:
                System.out
                        .println("BFS_BUILD -> BFS_BUILD_CHILD_REQUEST_REJECTED from " + latestMessage.getSenderUID());

                this.receivedAckMessagesCount = this.receivedAckMessagesCount + 1;

                if (this.didAllNeighboursReplyAck()) {
                    this.sendBeginChildSearch();
                    this.receivedAckMessagesCount = 0;
                }
                break;

            case BFS_BUILD_BEGIN_CHILD_SEARCH:
                System.out.println("BFS_BUILD -> BFS_BUILD_BEGIN_CHILD_SEARCH from " + latestMessage.getSenderUID());
                this.sendSearch();
                break;

            case BFS_BUILD_CHILD_SEARCH_COMPLETED:
                System.out
                        .println("BFS_BUILD -> BFS_BUILD_CHILD_SEARCH_COMPLETED from " + latestMessage.getSenderUID());

                this.receivedCompletedMessagesCount = this.receivedCompletedMessagesCount + 1;

                if (this.receivedCompletedMessagesCount == this.currNode.getChildNodes().size()) {
                    this.sendChildSearchComplete();
                }
                break;

            default:
                return;
        }
    }

    private boolean isTreeConstructionCompleted() {
        int rejectedNodesCount = this.rejectedNodes.size();
        int completedChildNodesCount = this.receivedCompletedMessagesCount;
        int parentsCount = this.currNode.isNodeLeader() ? 0 : 1;

        return this.currNode.getNeighbours().size() == rejectedNodesCount + completedChildNodesCount + parentsCount;
        // for (Map.Entry<Integer, Message.MessageType> entry :
        // neighboursLastComm.entrySet()) {
        // int neighbourUID = entry.getKey();
        // Message.MessageType messageType = entry.getValue();

        // if (neighbourUID == this.currNode.getParentUID()) {
        // if (messageType != Message.MessageType.BFS_BUILD_BEGIN_CHILD_SEARCH)
        // return false;
        // } else if (this.currNode.isNodeChild(neighbourUID)) {
        // if (messageType != Message.MessageType.BFS_BUILD_CHILD_SEARCH_COMPLETED)
        // return false;
        // } else {
        // if (messageType != Message.MessageType.BFS_BUILD_CHILD_REQUEST_REJECTED &&
        // this.rejectedNodes.indexOf(neighbourUID) == -1)
        // return false;
        // }
        // }
        // return true;
    }

    private boolean didAllNeighboursReplyAck() {
        return this.currNode.isNodeLeader()
                ? this.receivedAckMessagesCount == this.currNode.getNeighbours().size()
                : this.receivedAckMessagesCount + 1 == this.currNode.getNeighbours().size();
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

        this.rejectedNodes.add(clientUID);
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
        if (this.currNode.getParentUID() == -1)
            return;

        TCPClient client = this.currNode.getClientConnection(this.currNode.getParentUID());

        new Message(this.currNode.getUID(), this.currNode.getParentUID(),
                Message.MessageType.BFS_BUILD_CHILD_SEARCH_COMPLETED)
                .send(client.getServerNode(), client.getOutputStream());
    }
}
