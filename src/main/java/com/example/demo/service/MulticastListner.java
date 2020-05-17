package com.example.demo.service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.io.IOException;
import java.util.ArrayList;

public class MulticastListner implements Runnable {

    RestNodeService nodeService;


    public MulticastListner(RestNodeService temp) throws UnknownHostException {
        nodeService = temp;
    }


    private ArrayList<String> getNameAndIp(String msg) throws IOException, InterruptedException {
        System.out.println("ik run nu get name and ip met msg " + msg);
        ArrayList<String> temp = new ArrayList<>();
        if (msg.contains("newNode")) {
            System.out.println("Mijne setupB is hier "+nodeService.setupb);
            String haha = msg.replace("newNode ", "");
            if (!haha.isEmpty()) {
                String[] tokens = haha.split("::");
                for (String t : tokens)
                    temp.add(t);
            }
        if (nodeService.setupb) {
            if (nodeService.first) {
                System.out.println("de tweede is erbij");
                Thread.sleep(500);
                //Hier rest shit set previous
                //sendUDPMessage("previous " + name + "::ip " + thisIp, temp.get(1), 10000);
                URL connection = new URL("http://" + temp.get(1) + ":9000/SetPrevious?name=" + nodeService.name + "&ip=" + nodeService.thisIp);
                //Mogenlijk Zo reply opvangen?
                connection.openConnection().getInputStream();
                //
                //sendUDPMessage("next " + name + "::ip " + thisIp, temp.get(1), 10000);
                URL connection2 = new URL("http://" + temp.get(1) + ":9000/SetNext?name=" + nodeService.name + "&ip=" + nodeService.thisIp);
                //
                connection2.openConnection().getInputStream();
                //

                nodeService.next = temp.get(0);
                nodeService.nextIP = temp.get(1);
                nodeService.previous = temp.get(0);
                nodeService.previousIP = temp.get(1);
                System.out.println("Mijne next is nu " + nodeService.next + " " + nodeService.nextIP);
                System.out.println("Mijne previous is nu " + nodeService.previous + " " + nodeService.previousIP);
                nodeService.first = false;
            } else {
                if (nodeService.hashfunction(nodeService.name, true) < nodeService.hashfunction(temp.get(0), true) && nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.next, true)) {
                    Thread.sleep(500);
                    URL connection = new URL("http://" + temp.get(1) + ":9000/SetPrevious?name=" + nodeService.name + "&ip=" + nodeService.thisIp);
                    //
                    connection.openConnection().getInputStream();
                    //
                    nodeService.next = temp.get(0);
                    nodeService.nextIP = temp.get(1);
                    System.out.println("Mijne next is nu " + nodeService.next + " " + nodeService.nextIP);
                    System.out.println("Mijne previous is nu " + nodeService.previous + " " + nodeService.previousIP);
                }
                if (nodeService.hashfunction(nodeService.previous, true) < nodeService.hashfunction(temp.get(0), true) && nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.name, true)) {
                    Thread.sleep(500);
                    URL connection2 = new URL("http://" + temp.get(1) + ":9000/SetNext?name=" + nodeService.name + "&ip=" + nodeService.thisIp);
                    connection2.openConnection().getInputStream();
                    //haha
                    nodeService.previous = temp.get(0);
                    nodeService.previousIP = temp.get(1);
                    System.out.println("Mijne next is nu " + nodeService.next + " " + nodeService.nextIP);
                    System.out.println("Mijne previous is nu " + nodeService.previous + " " + nodeService.previousIP);
                }
            }
        }
        return temp;
    }
        System.out.println("FOUTJJJJJJJEEEEEEEEEEEEEEEEEEEEEEEEE");
        return null;
    }
    public void receiveUDPMessage(String ip, int port) throws
            IOException, InterruptedException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);
        while (true) {
            System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer,
                    buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(),
                    packet.getOffset(), packet.getLength());
            System.out.println("Ik krijg multicast "+msg);
            if (msg.contains("nodeCount"))
                nodeService.setUp(msg);
            if (msg.contains("newNode"))
                getNameAndIp(msg);

            //Dees Ga Ook Weg Moete
            if ("shutdown".equals(msg)) {
                nodeService.shutdown();
                break;
            }
        }
        socket.leaveGroup(group);
        socket.close();
    }

    @Override
    public void run() {
        try {
            receiveUDPMessage("230.0.0.0", 10000);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}

