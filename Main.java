import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        try {
            String currHostName = InetAddress.getLocalHost().getHostName();
            Node currNode = ReadConfig.read(currHostName);

            Runnable servRunnable = new Runnable() {
                public void run() {
                    TCPServer server = new TCPServer(currNode);
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
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            TCPClient client = new TCPClient(currNode, neighbourNode.getHostName(),
                                    neighbourNode.getPort());
                            client.connect();
                        }
                    };
                    Thread cliThread = new Thread(cliRunnable);
                    cliThread.start();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
