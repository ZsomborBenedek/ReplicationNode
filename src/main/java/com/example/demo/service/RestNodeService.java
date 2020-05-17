package com.example.demo.service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RestNodeService {

    InetAddress inetAddress = InetAddress.getLocalHost();
    public String name = inetAddress.getHostName();
    public String thisIp =inetAddress.getHostAddress();
    String previous;
    String next;
    String previousIP = "";
    String nextIP = "";
    String nameServerIP;
    boolean setupb = false;
    ArrayList<String> files = new ArrayList<>();
    boolean first = false;
    boolean running = true;

    public RestNodeService() throws IOException {
        sendUDPMessage("newNode "+name+"::"+thisIp, "230.0.0.0",10000);
        System.out.println("dees is mijn naam "+name);
        System.out.println("dees is mijn ip "+thisIp);
        //Recieve reply van Namingserver
        /*
        */
        //For lus da alle files een voor een naar de naming server stuurt
        System.out.println("Opgestart");
        //False gezet in declatatie nu ipv constructor, niet zeker
        //setupb = false;
    }
    @PostConstruct


    //Send UDP Messages
    public static void sendUDPMessage(String message,
                                      String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                group, port);
        socket.send(packet);
        socket.close();
    }
    //hahahaha

    //Parse message to set up new next node
    public void next(String name, String ip){
        if (!name.isEmpty() && !ip.isEmpty()) {
            next = name;
            nextIP = ip;
            System.out.println("Ik stel previous nu in als "+name+" "+ip);
            System.out.println("Mijne previous is nu "+next+" "+nextIP);
        }
    }


    //Parse message to set up new previous node
    public void previous(String name, String ip){
        if (!name.isEmpty() && !ip.isEmpty()) {
            previous = name;
            previousIP = ip;
            System.out.println("Ik stel previous nu in als "+name+" "+ip);
            System.out.println("Mijne previous is nu "+previous+" "+previousIP);
        }
    }


    //Check locally stored files
    private void chekFiles() throws IOException {
        System.out.println("ik run nu chek files");
        //
        File folder = null;
        if (name.equals("host2"))
        folder = new File("/home/pi/ReplicationNode/src/localFilesHost1");
        if (name.equals("host3"))
            folder = new File("/home/pi/ReplicationNode/src/localFilesHost2");
        if (name.equals("host4"))
            folder = new File("/home/pi/ReplicationNode/src/localFilesHost3");
        if (name.equals("host5"))
            folder = new File("/home/pi/ReplicationNode/src/localFilesHost4");
        //File folder = new File("C:\\Users\\Arla\\Desktop\\RestfullNode\\src\\localFiles");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String bestand = listOfFiles[i].getName().replace("Files\\","");
                String[] temp = bestand.split("\\.");
                files.add(temp[0]);
                System.out.println("Ik heb file "+temp[0]+" lokaal staan bruur.");
                URL connection2 = new URL("http://"+nameServerIP+":10000/AddFile?Name="+name+"&File="+temp[0]);
                connection2.openConnection().getInputStream();
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }
    private void chekReplicatedFiles(){
        File folder = new File("/home/pi/ReplicationNode/src/replicatedFiles");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String bestand = listOfFiles[i].getName().replace("Files\\","");
                String[] temp = bestand.split(".");
                files.add(temp[0]);
                System.out.println("Ik heb file "+temp[0]+" lokaal staan bruur.");

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }
    public void addReplicatedFile(File newFile, String name){
        // FILE FILE met filepath, chek als file al bestaat-> delete, binnegekrege file aanmaken.

    }


    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //Hier mogenlijk bool om folder te bepalen
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public void recieveTCP(String ip, String filename) throws IOException {
        byte [] b = new byte[5000];
        Socket sr = new Socket(ip,6667);
        InputStream is = sr.getInputStream();
        FileOutputStream fr = new FileOutputStream("/home/pi/ReplicationNode/src/replicatedFiles"+filename);
        is.read(b,0,b.length);
        fr.write(b,0,b.length);
    }

    //ShutDown
    public void addToNameServer(String ip) throws IOException {
        System.out.println("Ik run nu addToNameServer, Variebelen ip vn nameserver "+ip);
        nameServerIP = ip;
        URL connection2 = new URL("http://"+ip+":10000/AddNode?Name="+name+"&Ip="+thisIp);
        connection2.openConnection().getInputStream();
        chekFiles();
    }
    public void shutdown() throws IOException {

        //
        URL connection2 = new URL("http://"+previousIP+":10000/SetNext?Name="+next+"&ip="+nextIP);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection2.openStream()));
        String message = in.readLine();
        System.out.println(message);
        //

        URL connection = new URL("http://"+nextIP+":10000/SetPrevious?Name="+previous+"&ip="+previousIP);
        //
        connection.openConnection().getInputStream();
        //

        for (String file : files){
            sendUDPMessage("File "+file,previousIP,10000);
        }
        running = false;
        System.out.println("thread shut down");
    }


    void setUp(String msg){
        String haha = msg.replace("nodeCount ","");
        if(Integer.parseInt(haha)<=1){
            System.out.println("ik ben de eerste");
            next = previous = name;
            nextIP = previousIP = thisIp;
            first = true;
        }
        setupb = true;
    }

    //Hashfunction, boolean specifies if the string is a node or not
    public int hashfunction(String name, boolean node) {
        int hash=0;
        int temp = 0;
        int i;
        for (i = 0; i<name.length();i++) {
            hash = 3 * hash + name.charAt(i);
            temp = temp+ name.charAt(i);
        }
        hash = hash/(temp/7);

        if (node) {
            hash = (hash) / (5);
        }
        else
            hash = hash/53;
        return hash;
    }
}
