import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ReadConfig {
    public static Node read(String currHostName) throws Exception {
        FileReader fr = new FileReader(
                "./config.txt");
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

        for (Node nodei : nodesList) {
            if (currHostName.split("\\.")[0].equals(nodei.getHostName())) {
                nodei.setAllNodes(nodesList);
                return nodei;
            }
        }

        return new Node();
    }
}
