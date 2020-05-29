package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import com.example.demo.model.FileModel;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class FileChecker implements Runnable {

    RestNodeService nodeService;
    ArrayList<String> files;
    RestTemplate restTemplate;

    public FileChecker(RestNodeService nodeService) {
        this.nodeService = nodeService;
        this.files = new ArrayList<>();
        this.restTemplate = new RestTemplate();
    }

    public void chekFiles() throws InterruptedException, IOException {
        Thread.sleep(10000);
        System.out.println("Running Filechecker...");
        if (!files.isEmpty()) {
            files.clear();
        }

        File folder = null;

        if (nodeService.name.equals("host2"))
            folder = new File("src/localFilesHost1");
        if (nodeService.name.equals("host3"))
            folder = new File("src/LocalFilesHost2");
        if (nodeService.name.equals("host4"))
            folder = new File("src/LocalFilesHost3");
        if (nodeService.name.equals("host5"))
            folder = new File("src/LocalFilesHost4");

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String bestand = listOfFiles[i].getName().replace("Files\\", "");
                String[] temp = bestand.split("\\.");
                files.add(temp[0]);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        if (!nodeService.files.equals(files)) {
            if (files.size() > nodeService.files.size()) {
                System.out.println("Need to add some files");
                nodeService.chekFiles();
            }

            if (files.size() <= nodeService.files.size()) {
                System.out.println("Need to remove some files");
                ArrayList<String> temp = nodeService.files;
                temp.removeAll(files);
                for (String s : temp) {
                    String url = "http://" + nodeService.nameServerIP + ":10000/RemoveFile";
                    FileModel file = new FileModel(nodeService.name, s);
                    ResponseEntity<FileModel> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<FileModel>(file), FileModel.class);
                    System.out.println(response.toString());
                }
                nodeService.chekFiles();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
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
