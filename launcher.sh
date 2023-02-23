#!/bin/bash

# Change this to your netid
netid=nxv210002

# Root directory of your project
PROJECT_DIR="D:/Coursework/CS 6380 - DC/Assignments/Assignment 01/Submission/Distributed-Systems"

# Directory where the config file is located on your local system
CONFIG_LOCAL="D:/Coursework/CS 6380 - DC/Assignments/Assignment 01/Submission/Distributed-Systems/config.txt"

# Directory your java classes are in
BINARY_DIR="$PROJECT_DIR/bin"

# Your main project class
PROGRAM=Main

n=0
wt PowerShell.exe -NoExit -Command "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@dc01.utdallas.edu ls -a"
cat "$CONFIG_LOCAL" | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i

    while [[ $n -lt $i ]]
    do
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )

        # wt PowerShell.exe -NoExit -Command "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host.utdallas.edu ls -a"
	    # gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host.utdallas.edu java -cp $BINARY_DIR $PROGRAM $p; exec bash" &

        n=$(( n + 1 ))
    done
)
