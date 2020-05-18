package com.example.demo.service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPListner implements Runnable{
    RestNodeService nodeService;
    boolean mode;
    String file;
    public TCPListner(RestNodeService nodeService, boolean mode, String file){
        this.nodeService = nodeService;
        this.mode = mode;
        this.file = file;
    }
    public void sendTCP() throws IOException {
        System.out.println("Ik run nu send tcp met veriabelen mode = "+mode+" en file "+file);
        ServerSocket s = new ServerSocket(6969);
        Socket sr = s.accept();
        FileInputStream fr = null;
        if(nodeService.name.equals("host2"))
            fr = new FileInputStream("/home/pi/ReplicationNode/src/localFilesHost1/"+file);
        if(nodeService.name.equals("host3"))
            fr = new FileInputStream("/home/pi/ReplicationNode/src/LocalFilesHost2/"+file);
        if(nodeService.name.equals("host4"))
            fr = new FileInputStream("/home/pi/ReplicationNode/src/LocalFilesHost3/"+file);
        if(nodeService.name.equals("host5"))
            fr = new FileInputStream("/home/pi/ReplicationNode/src/LocalFilesHost4/"+file);

        byte b[] = new byte[5000];
        fr.read(b,0,b.length);
        OutputStream os = sr.getOutputStream();
        os.write(b,0,b.length);
        System.out.println("versture is kleir");
        s.close();
    }
    public void transferTCP() throws IOException {
        System.out.println("Ik run nu transfer tcp met veriabelen mode = "+mode+" en file "+file);
        ServerSocket s = new ServerSocket(6667);
        Socket sr = s.accept();
        FileInputStream fr = null;
        //if(nodeService.name.equals("host2"))
        fr = new FileInputStream("/home/pi/ReplicationNode/src/replicatedFiles/"+file);
        byte b[] = new byte[5000];
        fr.read(b,0,b.length);
        OutputStream os = sr.getOutputStream();
        os.write(b,0,b.length);
        File temp = new File("/home/pi/ReplicationNode/src/replicatedFiles/"+file);
        temp.delete();
        System.out.println("transfer is kleir");
        s.close();
    }

    @Override
    public void run() {
        if(mode) {
            try {
                sendTCP();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                transferTCP();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
