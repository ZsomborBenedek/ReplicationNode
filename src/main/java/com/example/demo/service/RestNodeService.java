package com.example.demo.service;

import javax.annotation.PostConstruct;
import com.example.demo.model.FileModel;
import com.example.demo.model.NodeModel;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class RestNodeService {

    InetAddress inetAddress = InetAddress.getLocalHost();
    public String name = inetAddress.getHostName();
    public String thisIp = inetAddress.getHostAddress();
    String previous;
    String next;
    String previousIP = "";
    String nextIP = "";
    String nameServerIP;
    boolean setupb = false;
    ArrayList<String> files = new ArrayList<>();
    boolean first = false;
    boolean running = true;
    boolean isHoogste = false;
    boolean isLaagste = false;
    RestTemplate restTemplate = new RestTemplate();

    public RestNodeService() throws IOException {
        sendUDPMessage("newNode " + name + "::" + thisIp, "230.0.0.0", 10000);
        System.out.println("My name is " + name);
        System.out.println("My ip is" + thisIp);
    }

    @PostConstruct

    // Send UDP Messages
    public static void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
        socket.send(packet);
        socket.close();
    }

    // Parse message to set up new next node
    public void next(String name, String ip) {
        if (!name.isEmpty() && !ip.isEmpty()) {
            next = name;
            nextIP = ip;
            System.out.println("my new next is " + next + " " + nextIP);
        }
    }

    // Parse message to set up new previous node
    public void previous(String name, String ip) {
        if (!name.isEmpty() && !ip.isEmpty()) {
            previous = name;
            previousIP = ip;
            System.out.println("my new previous is " + previous + " " + previousIP);
        }
    }

    // Check locally stored files
    public void chekFiles() throws IOException {
        files.clear();
        File folder = null;
        if (name.equals("host2"))
            folder = new File("src/localFilesHost1");
        if (name.equals("host3"))
            folder = new File("src/LocalFilesHost2");
        if (name.equals("host4"))
            folder = new File("src/LocalFilesHost3");
        if (name.equals("host5"))
            folder = new File("src/LocalFilesHost4");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String bestand = listOfFiles[i].getName().replace("Files\\", "");
                String[] temp = bestand.split("\\.");
                files.add(temp[0]);
                System.out.println(bestand + "is locally stored");

                String url = "http://" + nameServerIP + ":10000/AddFile";
                FileModel file = new FileModel(name, bestand);
                ResponseEntity<FileModel> response = restTemplate.postForEntity(url, new HttpEntity<FileModel>(file),
                        FileModel.class);
                System.out.println("\n" + response.toString() + "\n" + "\n");

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }

    private void chekReplicatedFiles() {
        File folder = new File("src/replicatedFiles");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String bestand = listOfFiles[i].getName().replace("Files\\", "");
                String[] temp = bestand.split(".");
                files.add(temp[0]);
                System.out.println("Ik heb file " + temp[0] + " lokaal staan bruur.");

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }

    public void addReplicatedFile(File newFile, String name) {
        // FILE FILE met filepath, chek als file al bestaat-> delete, binnegekrege file
        // aanmaken.

    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // Hier mogenlijk bool om folder te bepalen
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public void recieveTCP(String ip, String filename) throws IOException, InterruptedException {
        System.out.println("ik run nu receive tcp met variabelen ip " + ip + " filename " + filename);
        byte[] b = new byte[5000];
        Socket sr = new Socket(ip, 6969);
        InputStream is = sr.getInputStream();
        FileOutputStream fr = new FileOutputStream(new File("src/replicatedFiles/").getAbsolutePath() + filename);
        is.read(b, 0, b.length);
        fr.write(b, 0, b.length);
        System.out.println("File " + filename + " Recieved");
        sr.close();
    }

    // ShutDown
    public void addToNameServer(String ip) throws IOException {
        nameServerIP = ip;
        String url = "http://" + ip + ":10000/AddNode";
        NodeModel node = new NodeModel(name, thisIp);
        ResponseEntity<NodeModel> response = restTemplate.postForEntity(url, new HttpEntity<NodeModel>(node),
                NodeModel.class);
        System.out.println("\n" + response.toString() + "\n");

        chekFiles();
    }

    public void shutdown() throws IOException {

        //
        String urlNext = "http://" + previousIP + ":10000/SetNext";
        NodeModel nextNode = new NodeModel(next, nextIP);
        ResponseEntity<NodeModel> responseNext = restTemplate.exchange(urlNext, HttpMethod.PUT,
                new HttpEntity<NodeModel>(nextNode), NodeModel.class);
        System.out.println("\n" + responseNext.toString() + "\n");

        String urlPrevious = "http://" + nextIP + ":10000/SetPrevious";
        NodeModel previousNode = new NodeModel(previous, previousIP);
        ResponseEntity<NodeModel> responsePrev = restTemplate.exchange(urlPrevious, HttpMethod.PUT,
                new HttpEntity<NodeModel>(previousNode), NodeModel.class);
        System.out.println("\n" + responsePrev.toString() + "\n");

        for (String file : files) {
            sendUDPMessage("File " + file, previousIP, 10000);
        }
        running = false;
        System.out.println("thread shut down");
    }

    void setUp(String msg) {
        String haha = msg.replace("nodeCount ", "");
        if (Integer.parseInt(haha) <= 1) {
            System.out.println("I am  First");
            System.out.println("I am the highest hashed node");
            System.out.println("I am the lowest hashed node");
            next = previous = name;
            nextIP = previousIP = thisIp;
            first = true;
            isLaagste = true;
            isHoogste = true;
        }
        setupb = true;
    }

    public void setHighest() throws IOException {
        System.out.println("I am the highest hashed node");
        isHoogste = true;

        String urlPrevious = "http://" + nextIP + ":9000/SetPrevious";
        NodeModel previousNode = new NodeModel(name, thisIp);
        ResponseEntity<NodeModel> responsePrev = restTemplate.exchange(urlPrevious, HttpMethod.PUT,
                new HttpEntity<NodeModel>(previousNode), NodeModel.class);
        System.out.println("\n" + responsePrev.toString() + "\n");
    }

    public void setLowest() throws IOException {
        System.out.println("I am the lowest hashed node");
        isLaagste = true;

        String urlNext = "http://" + previousIP + ":9000/SetNext";
        NodeModel nextNode = new NodeModel(name, thisIp);
        ResponseEntity<NodeModel> responseNext = restTemplate.exchange(urlNext, HttpMethod.PUT,
                new HttpEntity<NodeModel>(nextNode), NodeModel.class);
        System.out.println("\n" + responseNext.toString() + "\n");
    }

    // Hashfunction, boolean specifies if the string is a node or not
    public int hashfunction(String name, boolean node) {
        int hash = 0;
        int temp = 0;
        int i;
        for (i = 0; i < name.length(); i++) {
            hash = 3 * hash + name.charAt(i);
            temp = temp + name.charAt(i);
        }
        hash = hash + temp;
        if (node) {
        } else
            hash = hash / 53;
        return hash;
    }

    public void removeReplicatedFile(String file) {
        File temp = new File("src/replicatedFiles/" + file);
        temp.delete();
        System.out.println("Replicated File file " + file + " is verwijderd");
    }
}
