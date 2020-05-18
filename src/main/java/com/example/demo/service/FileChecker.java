package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class FileChecker implements Runnable {
    RestNodeService nodeService;
    ArrayList<String> files = new ArrayList<>();
    public FileChecker(RestNodeService nodeService){
        this.nodeService = nodeService;
    }
    public void chekFiles() throws InterruptedException, IOException {
        Thread.sleep(10000);
            System.out.println("running Filechecker ");
            if(!files.isEmpty()){files.clear();}
            File folder = null;
            if (nodeService.name.equals("host2"))
                folder = new File("/home/pi/ReplicationNode/src/localFilesHost1");
            if (nodeService.name.equals("host3"))
                folder = new File("/home/pi/ReplicationNode/src/LocalFilesHost2");
            if (nodeService.name.equals("host4"))
                folder = new File("/home/pi/ReplicationNode/src/LocalFilesHost3");
            if (nodeService.name.equals("host5"))
                folder = new File("/home/pi/ReplicationNode/src/LocalFilesHost4");
            //File folder = new File("C:\\Users\\Arla\\Desktop\\RestfullNode\\src\\localFiles");
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String bestand = listOfFiles[i].getName().replace("Files\\","");
                    String[] temp = bestand.split("\\.");
                    files.add(temp[0]);
                } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
            if (!nodeService.files.equals(files)){
                if(files.size()>nodeService.files.size()) {
                    System.out.println("Der is iet bijgekome peinsk");
                    nodeService.chekFiles();
                }
                if(files.size()<=nodeService.files.size()){
                    System.out.println("Der is iet verdwene seg");
                    ArrayList<String> temp = nodeService.files;
                    temp.removeAll(files);
                    for(String s : temp) {
                        URL connection2 = new URL("http://" + nodeService.nameServerIP + ":10000/RemoveFile?Name=" + nodeService.name + "&File=" + s);
                        connection2.openConnection().getInputStream();
                    }
                    nodeService.chekFiles();
                }
            }
        }
    @Override
    public void run() {
        while(true){
            try {
                chekFiles();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
