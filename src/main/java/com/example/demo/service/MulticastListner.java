package com.example.demo.service;

import java.net.*;
import java.io.IOException;
import java.util.ArrayList;

import com.example.demo.model.NodeModel;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class MulticastListner implements Runnable {

    RestNodeService nodeService;
    RestTemplate restTemplate;

    public MulticastListner(RestNodeService temp) throws UnknownHostException {
        this.nodeService = temp;
        this.restTemplate = new RestTemplate();
    }

    private ArrayList<String> getNameAndIp(String msg) throws IOException, InterruptedException {
        ArrayList<String> temp = new ArrayList<>();
        if (msg.contains("newNode")) {
            String haha = msg.replace("newNode ", "");
            if (!haha.isEmpty()) {
                String[] tokens = haha.split("::");
                for (String t : tokens)
                    temp.add(t);
            }
            if (nodeService.setupb) {
                if (nodeService.first) {
                    System.out.println("Second node present");
                    Thread.sleep(500);

                    // Hier rest set previous
                    // sendUDPMessage("previous " + name + "::ip " + thisIp, temp.get(1), 10000);
                    String urlPrevious = "http://" + temp.get(1) + ":9000/SetPrevious";
                    NodeModel previousNode = new NodeModel(nodeService.name, nodeService.thisIp);
                    restTemplate.exchange(urlPrevious, HttpMethod.PUT, new HttpEntity<NodeModel>(previousNode), NodeModel.class);
                    // Mogenlijk Zo reply opvangen?
                    //
                    // sendUDPMessage("next " + name + "::ip " + thisIp, temp.get(1), 10000);
                    String urlNext = "http://" + temp.get(1) + ":9000/SetNext";
                    NodeModel nextNode = new NodeModel(nodeService.name, nodeService.thisIp);
                    restTemplate.exchange(urlNext, HttpMethod.PUT, new HttpEntity<NodeModel>(nextNode), NodeModel.class);

                    if (nodeService.isHoogste)
                        if (nodeService.hashfunction(temp.get(0), true) > nodeService.hashfunction(nodeService.name,
                                true)) {
                            System.out.println(temp.get(0) + " is the new highest node");
                            nodeService.isHoogste = false;

                            String isHighUrl = "http://" + temp.get(1) + ":9000/IsHighest";
                            restTemplate.exchange(isHighUrl, HttpMethod.PUT, new HttpEntity<String>("true"), NodeModel.class);
                        }
                    if (nodeService.isLaagste)
                        if (nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.name,
                                true)) {
                            System.out.println(temp.get(0) + " is the new lowest node");
                            nodeService.isLaagste = false;

                            String isLowUrl = "http://" + temp.get(1) + ":9000/IsLowest";
                            restTemplate.exchange(isLowUrl, HttpMethod.PUT, new HttpEntity<String>("true"), NodeModel.class);
                        }
                    nodeService.next = temp.get(0);
                    nodeService.nextIP = temp.get(1);
                    nodeService.previous = temp.get(0);
                    nodeService.previousIP = temp.get(1);
                    System.out.println("Next node is " + nodeService.next + " " + nodeService.nextIP);
                    System.out.println("Previous node is " + nodeService.previous + " " + nodeService.previousIP);
                    nodeService.first = false;
                } else {
                    if (nodeService.isHoogste)
                        if (nodeService.hashfunction(temp.get(0), true) > nodeService.hashfunction(nodeService.name,
                                true)) {
                            System.out.println(temp.get(0) + " is the new highest node");
                            nodeService.isHoogste = false;

                            Thread.sleep(500);

                            String urlPrevious = "http://" + temp.get(1) + ":9000/SetPrevious";
                            NodeModel previousNode = new NodeModel(nodeService.name, nodeService.thisIp);
                            restTemplate.exchange(urlPrevious, HttpMethod.PUT, new HttpEntity<NodeModel>(previousNode), NodeModel.class);

                            String urlNext = "http://" + temp.get(1) + ":9000/SetNext";
                            NodeModel nextNode = new NodeModel(nodeService.next, nodeService.nextIP);
                            restTemplate.exchange(urlNext, HttpMethod.PUT, new HttpEntity<NodeModel>(nextNode), NodeModel.class);

                            String isHighUrl = "http://" + temp.get(1) + ":9000/IsHighest";
                            restTemplate.exchange(isHighUrl, HttpMethod.PUT, new HttpEntity<String>("true"), NodeModel.class);

                            nodeService.next = temp.get(0);
                            nodeService.nextIP = temp.get(1);
                            System.out.println("My new next is  " + nodeService.next + " " + nodeService.nextIP);
                        }
                    if (nodeService.isLaagste)
                        if (nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.name,
                                true)) {
                            System.out.println(temp.get(0) + " is the new lowest node");
                            nodeService.isLaagste = false;

                            Thread.sleep(500);

                            String urlNext = "http://" + temp.get(1) + ":9000/SetNext";
                            NodeModel nextNode = new NodeModel(nodeService.next, nodeService.thisIp);
                            restTemplate.exchange(urlNext, HttpMethod.PUT, new HttpEntity<NodeModel>(nextNode), NodeModel.class);

                            String urlPrevious = "http://" + temp.get(1) + ":9000/SetPrevious";
                            NodeModel previousNode = new NodeModel(nodeService.previous, nodeService.previousIP);
                            restTemplate.exchange(urlPrevious, HttpMethod.PUT, new HttpEntity<NodeModel>(previousNode), NodeModel.class);

                            String isLowUrl = "http://" + temp.get(1) + ":9000/IsLowest";
                            restTemplate.exchange(isLowUrl, HttpMethod.PUT, new HttpEntity<String>("true"), NodeModel.class);
                            ;

                            nodeService.previous = temp.get(0);
                            nodeService.previousIP = temp.get(1);
                            System.out.println(
                                    "My new previous is " + nodeService.previous + " " + nodeService.previousIP);
                        }

                    if (nodeService.hashfunction(nodeService.name, true) < nodeService.hashfunction(temp.get(0), true)
                            && nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.next,
                                    true)) {
                        Thread.sleep(500);

                        String urlPrevious = "http://" + temp.get(1) + ":9000/SetPrevious";
                        NodeModel previousNode = new NodeModel(nodeService.name, nodeService.thisIp);
                        restTemplate.exchange(urlPrevious, HttpMethod.PUT, new HttpEntity<NodeModel>(previousNode), NodeModel.class);

                        //
                        nodeService.next = temp.get(0);
                        nodeService.nextIP = temp.get(1);
                        System.out.println("My new next is  " + nodeService.next + " " + nodeService.nextIP);
                        System.out.println("My new previous is " + nodeService.previous + " " + nodeService.previousIP);
                    }
                    if (nodeService.hashfunction(nodeService.previous, true) < nodeService.hashfunction(temp.get(0),
                            true)
                            && nodeService.hashfunction(temp.get(0), true) < nodeService.hashfunction(nodeService.name,
                                    true)) {
                        Thread.sleep(500);

                        String urlNext = "http://" + temp.get(1) + ":9000/SetNext";
                        NodeModel nextNode = new NodeModel(nodeService.name, nodeService.thisIp);
                        restTemplate.exchange(urlNext, HttpMethod.PUT, new HttpEntity<NodeModel>(nextNode), NodeModel.class);

                        // haha
                        nodeService.previous = temp.get(0);
                        nodeService.previousIP = temp.get(1);
                        System.out.println("My new next is  " + nodeService.next + " " + nodeService.nextIP);
                        System.out.println("My new previous is " + nodeService.previous + " " + nodeService.previousIP);
                    }
                }
            }
            return temp;
        }
        return null;
    }

    public void receiveUDPMessage(String ip, int port) throws IOException, InterruptedException {
        byte[] buffer = new byte[1024];
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);
        while (true) {
            // System.out.println("Waiting for multicast message...");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.println("Ik receive multicast " + msg);
            if (msg.contains("nodeCount"))
                nodeService.setUp(msg);
            if (msg.contains("newNode"))
                getNameAndIp(msg);

            // Dees Ga Ook Weg Moete
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
