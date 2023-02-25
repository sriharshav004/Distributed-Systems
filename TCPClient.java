import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCPClient {
    private Node clientNode;
    private Socket clientSocket;
    private ObjectInputStream inFromServer;
    private ObjectOutputStream outToServer;
    private Node serverNode;

    public TCPClient(Node clientNode, Node serverNode) {
        this.clientNode = clientNode;
        this.serverNode = serverNode;
    }

    public Node getClientNode() {
        return this.clientNode;
    }

    public ObjectOutputStream getOutputStream() {
        return this.outToServer;
    }

    public void connect() {
        try {
            this.clientSocket = new Socket(this.serverNode.getHostName(), this.serverNode.getPort());

            System.out.println(
                    "Client online connected UID: " + this.serverNode.getUID() + " and HostName: "
                            + this.serverNode.getHostName());

            this.outToServer = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.outToServer.flush();
            this.inFromServer = new ObjectInputStream(this.clientSocket.getInputStream());

            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(2000);

                    String text = "Hello from " + this.clientNode.getUID();
                    new Message(this.clientNode, text).send(this.serverNode, this.outToServer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
