import java.io.*;
import java.net.*;

public class TCPServer {
    private Node node;
    private ServerSocket serverSocket;

    public TCPServer(Node node) {
        this.node = node;
    }

    public void startListening() {
        try {
            serverSocket = new ServerSocket(this.node.getPort());

            System.out.println(
                    "Server online with UID: " + this.node.getUID() + " and HostName: " + this.node.getHostName());

            while (true) {
                Socket connectionSocket = serverSocket.accept();

                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
                            ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
                            outToClient.flush();

                            while (true) {
                                Message message = (Message) inFromClient.readObject();
                                System.out.println("Received message: " + message.getText() + " from client UID: "
                                        + connectionSocket.getRemoteSocketAddress());
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                };

                Thread t = new Thread(runnable);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
