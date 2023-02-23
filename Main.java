import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        String currHostName = "";
        try {
            currHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Node currNode = new Node();
        try {
            currNode = ReadConfig.read(currHostName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Server online with UID: " + currNode.getUID() + " and HostName: " + currNode.getHostName());
    }
}
