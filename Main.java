import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        try {
            String currHostName = InetAddress.getLocalHost().getHostName();
            Node currNode = ReadConfig.read(currHostName);

            TCPServer server = new TCPServer(currNode);
            Runnable servRunnable = new Runnable() {
                public void run() {
                    server.startListening();
                }
            };
            Thread servThread = new Thread(servRunnable);
            servThread.start();

            for (Node neighbourNode : currNode.getAllNodes()) {
                if (currNode.isNodeNeighbour(neighbourNode.getUID())) {
                    Runnable cliRunnable = new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            TCPClient client = new TCPClient(currNode, neighbourNode);
                            client.connect();

                            currNode.addNeighbourClient(client);
                        }
                    };
                    Thread cliThread = new Thread(cliRunnable);
                    cliThread.start();
                }
            }

            while (!currNode.areAllNeighboursOnline()) {
                System.out.println("LEADER_ELEC -> Waiting for all neighbours to come online");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            new PelegsLeaderElection(server).startElection();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new BFSTree(currNode).buildTree();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
