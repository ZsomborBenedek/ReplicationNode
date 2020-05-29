package com.example.demo.model;

public class NodeModel {

    String name;
    String ip;

    public NodeModel() {
    }

    public NodeModel(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public NodeModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return "Node{" 
        + "name='" + this.name + '\'' 
        + ", ip='" + this.ip + '\'' 
        + '}';
    }
}