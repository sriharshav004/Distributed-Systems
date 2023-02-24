import java.io.*;
import java.net.*;

public class TCPClient {
    private Socket clientSocket;
    private ObjectInputStream inFromServer;
    private Node node;
    private ObjectOutputStream outToServer;
    private String serverHostName;
    private int serverPortNumber;

    public TCPClient(Node node, String serverHostName, int serverPortNumber) {
        this.node = node;
        this.serverHostName = serverHostName;
        this.serverPortNumber = serverPortNumber;
    }

    public void connect() {
        try {
            this.clientSocket = new Socket(serverHostName, serverPortNumber);

            System.out.println(
                    "Client online with UID: " + this.node.getUID() + " and HostName: " + this.node.getHostName());

            this.outToServer = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.outToServer.flush();
            this.inFromServer = new ObjectInputStream(this.clientSocket.getInputStream());

            String text = "Hello from " + this.node.getUID();
            this.sendMessage(text, Message.MessageType.HANDSHAKE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String text, Message.MessageType type) {
        try {
            Message msg = new Message(text, type);

            this.outToServer.writeObject(msg);
            this.outToServer.flush();

            System.out.println("Sent message: " + msg.getText() + " to server UID: " + this.node.getUID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
