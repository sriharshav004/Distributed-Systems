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

            Runnable cliRunnable = new Runnable() {
                public void run() {
                    String serverHostName = currHostName == "dc02.utdallas.edu" ? "dc03.utdallas.edu"
                            : "dc02.utdallas.edu";
                    int serverPortNumber = currHostName == "dc02.utdallas.edu" ? 3233 : 2234;

                    TCPClient client = new TCPClient(currNode, serverHostName, serverPortNumber);
                    client.connect();
                }
            };
            Thread cliThread = new Thread(cliRunnable);
            cliThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
