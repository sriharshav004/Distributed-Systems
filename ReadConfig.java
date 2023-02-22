import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ReadConfig {
    public static ArrayList<Node> read() throws Exception {
        FileReader fr = new FileReader(
                "D:/Coursework/CS 6380 - DC/Assignments/Assignment 01/Submission/Distributed-Systems/config.txt");
        BufferedReader br = new BufferedReader(fr);

        ArrayList<Node> nodesList = new ArrayList<Node>();

        try {
            String thisLine = br.readLine();
            thisLine = br.readLine();
            int numberOfNodes = Integer.parseInt(thisLine);

            thisLine = br.readLine();
            thisLine = br.readLine();

            for (int i = 0; i < numberOfNodes; i++) {
                thisLine = br.readLine();

                String[] nodeInfo = thisLine.split(" ");
                Node newNode = new Node(Integer.parseInt(nodeInfo[0]), nodeInfo[1], Integer.parseInt(nodeInfo[2]));
                nodesList.add(newNode);
            }

            thisLine = br.readLine();
            thisLine = br.readLine();

            for (int i = 0; i < numberOfNodes; i++) {
                thisLine = br.readLine();

                String[] neighbours = thisLine.split(" ");
                for (String neighbourUID : neighbours) {
                    nodesList.get(i).addNeighbour(Integer.parseInt(neighbourUID));
                }
            }
        } finally {
            br.close();
        }

        return nodesList;
    }
}
