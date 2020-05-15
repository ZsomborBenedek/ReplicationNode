package com.example.demo.service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.io.IOException;
import java.util.ArrayList;

public class MulticastListner implements Runnable {

    RestNodeService nodeService;
    boolean setupb;
    boolean first = false;


    public MulticastListner(RestNodeService temp) throws UnknownHostException {
        nodeService = temp;
    }


    private ArrayList<String> getNameAndIp(String msg) throws IOException {
        ArrayList<String> temp = new ArrayList<>();
        if (msg.contains("newNode")) {
            String haha = msg.replace("newNode ", "");
            if (!haha.isEmpty()) {
                String[] tokens = haha.split("::");
                for (String t : tokens)
                    temp.add(t);
            }

        }
        if (setupb) {
            if (first) {
                System.out.println("de tweede is erbij");
                //Hier rest shit set previous
                //sendUDPMessage("previous " + name + "::ip " + thisIp, temp.get(1), 10000);
                URL connection = new URL("http://"+temp.get(1)+":10000/SetPrevious?Name="+nodeService.name+"&ip="+nodeService.thisIp);
                //Mogenlijk Zo reply opvangen?
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.openStream()));
                String message = in.readLine();
                System.out.println(message);
                //
                //sendUDPMessage("next " + name + "::ip " + thisIp, temp.get(1), 10000);
                URL connection2 = new URL("http://"+temp.get(1)+":10000/SetNext?Name="+nodeService.name+"&ip="+nodeService.thisIp);
                //
                BufferedReader in2 = new BufferedReader(new InputStreamReader(
                        connection2.openStream()));
                String message2 = in2.readLine();
                System.out.println(message2);
                //

                nodeService.next = temp.get(0);
                nodeService.nextIP = temp.get(1);
                nodeService.previous = temp.get(0);
                nodeService.previousIP = temp.get(1);
                System.out.println("Mijne next is nu "+nodeService.next+" "+nodeService.nextIP);
                System.out.println("Mijne previous is nu "+nodeService.previous+" "+nodeService.previousIP);
                first = false;
            } else {
                if (nodeService.hashfunction(nodeService.name, true) < nodeService.hashfunction(temp.get(0), true) && nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.next, true)) {
                    URL connection = new URL("http://"+temp.get(1)+":10000/SetPrevious?Name="+nodeService.name+"&ip="+nodeService.thisIp);
                    //
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection.openStream()));
                    String message = in.readLine();
                    System.out.println(message);
                    //
                    nodeService.next = temp.get(0);
                    nodeService.nextIP = temp.get(1);
                    System.out.println("Mijne next is nu "+nodeService.next+" "+nodeService.nextIP);
                    System.out.println("Mijne previous is nu "+nodeService.previous+" "+nodeService.previousIP);
                }
                if (nodeService.hashfunction(nodeService.previous, true) < nodeService.hashfunction(temp.get(0), true) && nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.name, true)) {
                    URL connection2 = new URL("http://"+temp.get(1)+":10000/SetNext?Name="+nodeService.name+"&ip="+nodeService.thisIp);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection2.openStream()));
                    String message = in.readLine();
                    System.out.println(message);
                    //
                    nodeService.previous = temp.get(0);
                    nodeService.previousIP = temp.get(1);
                    System.out.println("Mijne next is nu "+nodeService.next+" "+nodeService.nextIP);
                    System.out.println("Mijne previous is nu "+nodeService.previous+" "+nodeService.previousIP);
                }
            }
        }
        return temp;
    }
    public void receiveUDPMessage(String ip, int port) throws
            IOException {
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

