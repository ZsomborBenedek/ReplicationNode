package com.example.demo.service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPListner {
    public void sendTCP(String file) throws IOException {
        ServerSocket s = new ServerSocket(6667);
        Socket sr = s.accept();
        FileInputStream fr = new FileInputStream("src/"+file);

        byte b[] = new byte[5000];
        fr.read(b,0,b.length);
        OutputStream os = sr.getOutputStream();
        os.write(b,0,b.length);
    }
}
