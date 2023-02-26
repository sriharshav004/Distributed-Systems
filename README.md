# Distributed-Systems

Team Members: 

Name : Naga Sri Harsha Vadrevu
NetID: NXV210002

Name : Jayanth Avinash Potnuru
NetID: JXP220032



Algorithm's implemented: 

1.Peleg's algorithm for leader election
2.SynchBFS for cosntruction spanning tree rooted at elected leader.

Programming language: Java 

Flow: config.txt is read by each node in the Distributed system, UID along with neighboring node names and their port numbers are read along. TCP is used as communication protocol. 

Each node starts participating in the leader election protocol. Node with heighest UID is elected as leader. Once the leader is chosen, the leader starts SynchBFS algorithm to construct spanning tree.

In SynchBFS each node identifies it's parent node and children nodes. Note that communication in the network is bidirectional.


Compiliation order:

Node.java
ReadConfig.java
Message.java
TCPClient.java
TCPServer.java
PelegsLedeaderElection.java
BFSTree.java
Main.java