package com.example.demo.model;

public class FileModel {

    String file;
    String node;

    public FileModel(String node, String file) {
        this.node = node;
        this.file = file;
    }

    public FileModel(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    public String getNode() {
        return node;
    }
}