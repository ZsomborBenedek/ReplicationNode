package com.example.demo.service;

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
    boolean setupb;
    ArrayList<String> files = new ArrayList<>();
    boolean first = false;
    boolean running = true;

    public RestNodeService() throws IOException {
        sendUDPMessage("newNode "+name+"::"+thisIp, "230.0.0.0",10000);
        System.out.println("dees is mijn naam "+name);
        System.out.println("dees is mijn ip "+thisIp);
        //Recieve reply van Namingserver
        /*
        DOE Deze shit me Reply van naming server
        URL connection = new URL("http://localhost:8080/LocateFile?fileName=jej.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.openStream()));
        String message = in.readLine();
        System.out.println(message);
        */
        chekFiles();
        //For lus da alle files een voor een naar de naming server stuurt
        System.out.println("Opgestart");
        setupb = false;
    }


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
        File folder = new File("Files");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String bestand = listOfFiles[i].getName().replace("Files\\","");
                files.add(bestand);
                System.out.println("Ik heb file "+bestand+" lokaal staan bruur.");
                URL connection2 = new URL("http://"+nameServerIP+":10000/AddFile?Name="+name+"&File="+bestand);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection2.openStream()));
                String message = in.readLine();
                System.out.println(message);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }
    private void chekReplicatedFiles(){
        File folder = new File("Files");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String bestand = listOfFiles[i].getName().replace("Files\\","");
                files.add(bestand);
                System.out.println("Ik heb file "+bestand+" lokaal staan bruur.");

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }
    public void addReplicatedFile(File newFile, String name){
        // FILE FILE met filepath, chek als file al bestaat-> delete, binnegekrege file aanmaken.

    }

    public void recieveTCP(String ip, String filename) throws IOException {
        byte [] b = new byte[5000];
        Socket sr = new Socket(ip,6667);
        InputStream is = sr.getInputStream();
        FileOutputStream fr = new FileOutputStream("C:\\Users\\Arla\\Desktop\\School\\EenBekeTeste\\AlstHierKomtIstCool\\"+filename);
        is.read(b,0,b.length);
        fr.write(b,0,b.length);
    }

    //ShutDown
    public void addToNameServer(String ip) throws IOException {
        nameServerIP = ip;
        URL connection2 = new URL("http://"+ip+":10000/AddNode?Name="+name+"&ip="+thisIp);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection2.openStream()));
        String message = in.readLine();
        System.out.println(message);
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
        BufferedReader in2 = new BufferedReader(new InputStreamReader(
                connection.openStream()));
        String message2 = in2.readLine();
        System.out.println(message2);
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
