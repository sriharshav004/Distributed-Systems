import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BFSTree {
    private Node currNode;
    private HashMap<Integer, Message.MessageType> neighboursLastComm;
    private int receivedAckMessagesCount;
    private int receivedCompletedMessagesCount;
    private List<Integer> rejectedNeighbours;

    public BFSTree(Node currNode) {
        this.currNode = currNode;
        this.rejectedNeighbours = new ArrayList<Integer>();

        this.neighboursLastComm = new HashMap<>();
        for (int neighbourUID : this.currNode.getNeighbours()) {
            this.neighboursLastComm.put(neighbourUID, Message.MessageType.HANDSHAKE);
        }
    }

    public void buildTree() {
        System.out.println("BFS_BUILD -> Starting building BFS tree on node: " + this.currNode.getUID());

        this.currNode.startBFSBuild();
        if (this.currNode.isNodeLeader()) {
            this.sendSearch();
        }

        this.receivedAckMessagesCount = 0;
        this.receivedCompletedMessagesCount = 0;

        while (true) {
            this.listenMessages();

            if (this.isChildTreeCompleted())
                break;
        }

        System.out.println("BFS_BUILD -> Completed for UID: " + this.currNode.getUID() + ". Child Nodes: "
                + this.currNode.getChildNodes() + ". Degree: "
                + this.currNode.getDegree());
    }

    private void listenMessages() {
        Message latestMessage = this.currNode.popLatestReceivedMessage();
        if (latestMessage.getSenderUID() == -1)
            return;

        this.neighboursLastComm.put(latestMessage.getSenderUID(), latestMessage.getType());

        switch (latestMessage.getType()) {
            case BFS_ADD_CHILD_REQUEST:
                if (this.currNode.isNodeVisited()) {
                    this.sendAddChildRequestRejected(latestMessage.getSenderUID());
                } else {
                    this.currNode.setParent(latestMessage.getSenderUID());
                    this.currNode.setVisited(true);

                    this.sendAddChildRequestAccepted();
                }
                break;

            case BFS_ADD_CHILD_REQUEST_ACCEPTED:
                this.currNode.addChildNode(latestMessage.getSenderUID());
                this.receivedAckMessagesCount = this.receivedAckMessagesCount + 1;

                if (this.didAllNeighboursAck()) {
                    this.sendBeginChildSearch();
                    this.receivedAckMessagesCount = 0;
                }
                break;

            case BFS_ADD_CHILD_REQUEST_REJECTED:
                this.receivedAckMessagesCount = this.receivedAckMessagesCount + 1;

                if (this.didAllNeighboursAck()) {
                    this.sendBeginChildSearch();
                    this.receivedAckMessagesCount = 0;
                }
                break;

            case BFS_BEGIN_CHILD_SEARCH:
                this.sendSearch();
                break;

            case BFS_CHILD_BUILD_COMPLETED:
                this.receivedCompletedMessagesCount = this.receivedCompletedMessagesCount + 1;

                if (this.receivedCompletedMessagesCount == this.currNode.getChildNodes().size()) {
                    this.sendChildBuildComplete();
                }
                break;

            default:
                return;
        }
    }

    private boolean isChildTreeCompleted() {
        for (Map.Entry<Integer, Message.MessageType> entry : this.neighboursLastComm.entrySet()) {
            int neighbourUID = entry.getKey();
            Message.MessageType messageType = entry.getValue();

            if (neighbourUID == this.currNode.getParentUID()) {
                if (messageType != Message.MessageType.BFS_BEGIN_CHILD_SEARCH)
                    return false;
            } else if (this.currNode.isNodeChild(neighbourUID)) {
                if (messageType != Message.MessageType.BFS_CHILD_BUILD_COMPLETED)
                    return false;
            } else {
                if (messageType != Message.MessageType.BFS_ADD_CHILD_REQUEST_REJECTED &&
                        this.rejectedNeighbours.indexOf(neighbourUID) == -1)
                    return false;
            }
        }
        return true;
    }

    private boolean didAllNeighboursAck() {
        int rejectedNodesCount = this.rejectedNeighbours.size();
        int ackChildNodesCount = this.receivedAckMessagesCount;
        int parentsCount = this.currNode.isNodeLeader() ? 0 : 1;

        return rejectedNodesCount + ackChildNodesCount + parentsCount == this.currNode.getNeighbours().size();
    }

    private void sendSearch() {
        int messagesCount = 0;

        for (TCPClient client : this.currNode.getNeighbourClients()) {
            int clientUID = client.getServerNode().getUID();

            if (clientUID != this.currNode.getParentUID() && this.rejectedNeighbours.indexOf(clientUID) == -1) {
                new Message(this.currNode.getUID(), Message.MessageType.BFS_ADD_CHILD_REQUEST)
                        .send(client.getServerNode(), client.getOutputStream());

                messagesCount++;
            }
        }

        if (messagesCount == 0) {
            this.sendChildBuildComplete();
        }
    }

    private void sendAddChildRequestAccepted() {
        TCPClient client = this.currNode.getClientConnection(this.currNode.getParentUID());

        new Message(this.currNode.getUID(), Message.MessageType.BFS_ADD_CHILD_REQUEST_ACCEPTED)
                .send(client.getServerNode(), client.getOutputStream());
    }

    private void sendAddChildRequestRejected(int clientUID) {
        TCPClient client = this.currNode.getClientConnection(clientUID);

        new Message(this.currNode.getUID(), Message.MessageType.BFS_ADD_CHILD_REQUEST_REJECTED)
                .send(client.getServerNode(), client.getOutputStream());

        this.rejectedNeighbours.add(clientUID);

        int rejectedNodesCount = this.rejectedNeighbours.size();
        int completedChildNodesCount = this.receivedCompletedMessagesCount;
        int parentsCount = this.currNode.isNodeLeader() ? 0 : 1;

        if (rejectedNodesCount + completedChildNodesCount + parentsCount == this.currNode.getNeighbours().size()) {
            sendChildBuildComplete();
        }
    }

    private void sendBeginChildSearch() {
        int sentMessagesCount = 0;

        for (int childUID : this.currNode.getChildNodes()) {
            TCPClient client = this.currNode.getClientConnection(childUID);

            new Message(this.currNode.getUID(), Message.MessageType.BFS_BEGIN_CHILD_SEARCH).send(client.getServerNode(),
                    client.getOutputStream());

            sentMessagesCount++;
        }

        if (sentMessagesCount == 0) {
            this.sendChildBuildComplete();
        }
    }

    private void sendChildBuildComplete() {
        if (this.currNode.getParentUID() == -1)
            return;

        TCPClient client = this.currNode.getClientConnection(this.currNode.getParentUID());

        new Message(this.currNode.getUID(), Message.MessageType.BFS_CHILD_BUILD_COMPLETED).send(client.getServerNode(),
                client.getOutputStream());
    }
}
