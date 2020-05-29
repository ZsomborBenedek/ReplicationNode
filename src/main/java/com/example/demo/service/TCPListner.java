package com.example.demo.service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPListner implements Runnable {
    RestNodeService nodeService;
    boolean mode;
    String file;

    public TCPListner(RestNodeService nodeService, boolean mode, String file) {
        this.nodeService = nodeService;
        this.mode = mode;
        this.file = file;
    }

    public void sendTCP() throws IOException {
        ServerSocket s = new ServerSocket(6969);
        Socket sr = s.accept();
        FileInputStream fr = null;
        if (nodeService.name.equals("host2"))
            fr = new FileInputStream(new File("src/localFilesHost1/" + file).getAbsolutePath());
        if (nodeService.name.equals("host3"))
            fr = new FileInputStream(new File("src/LocalFilesHost2/" + file).getAbsolutePath());
        if (nodeService.name.equals("host4"))
            fr = new FileInputStream(new File("src/LocalFilesHost3/" + file).getAbsolutePath());
        if (nodeService.name.equals("host5"))
            fr = new FileInputStream(new File("src/LocalFilesHost4/" + file).getAbsolutePath());

        byte b[] = new byte[5000];
        fr.read(b, 0, b.length);
        OutputStream os = sr.getOutputStream();
        os.write(b, 0, b.length);
        System.out.println("Sending " + file + " completed");
        s.close();
    }

    public void transferTCP() throws IOException {
        ServerSocket s = new ServerSocket(6969);
        Socket sr = s.accept();
        FileInputStream fr = null;
        // if(nodeService.name.equals("host2"))
        fr = new FileInputStream(new File("src/replicatedFiles/" + file).getAbsolutePath());
        byte b[] = new byte[5000];
        fr.read(b, 0, b.length);
        OutputStream os = sr.getOutputStream();
        os.write(b, 0, b.length);
        File temp = new File(new File("src/replicatedFiles/" + file).getAbsolutePath());
        temp.delete();
        System.out.println("Sending " + file + " completed");
        s.close();
    }

    @Override
    public void run() {
        if (mode) {
            try {
                sendTCP();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                transferTCP();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
