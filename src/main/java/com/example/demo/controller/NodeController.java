package com.example.demo.controller;

import com.example.demo.model.FileModel;
import com.example.demo.model.NodeModel;
import com.example.demo.service.FileChecker;
import com.example.demo.service.MulticastListner;
import com.example.demo.service.RestNodeService;
import com.example.demo.service.TCPListner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class NodeController {

    RestNodeService nodeService;
    ExecutorService threadPool = Executors.newFixedThreadPool(5);

    public NodeController() throws IOException {
    }

    @PostConstruct
    public void init() throws IOException {
        nodeService = new RestNodeService();
        threadPool.execute(new MulticastListner(nodeService));
        threadPool.execute(new FileChecker(nodeService));
    }

    @PostMapping("/SetNameServer")
    public ResponseEntity<String> setNameServer(@RequestBody String ip) {
        if (!ip.isEmpty()) {
            System.out.println("running /SetNameServer, ip " + ip);
            try {
                nodeService.addToNameServer(ip);
            } catch (IOException e) {
                return new ResponseEntity<String>(ip, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
            return new ResponseEntity<String>(ip, HttpStatus.CREATED);
        }
        return new ResponseEntity<String>(ip, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/SetNext")
    public ResponseEntity<NodeModel> setNext(@RequestBody NodeModel node) {
        if (!node.getName().isEmpty() && !node.getIp().isEmpty()) {
            System.out.println("Running /SetNext, name " + node.getName() + " ip " + node.getIp());
            nodeService.next(node.getName(), node.getIp());
            return new ResponseEntity<NodeModel>(node, HttpStatus.OK);
        }
        return new ResponseEntity<NodeModel>(node, HttpStatus.BAD_REQUEST);

    }

    @PutMapping("/SetPrevious")
    public ResponseEntity<NodeModel> setPrevious(@RequestBody NodeModel node) {
        if (!node.getName().isEmpty() && !node.getIp().isEmpty()) {
            System.out.println("Running /SetPrevious, name " + node.getName() + " ip " + node.getIp());
            nodeService.previous(node.getName(), node.getIp());
            return new ResponseEntity<NodeModel>(node, HttpStatus.OK);
        }
        return new ResponseEntity<NodeModel>(node, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/GetReplicationFile/{fileName}/{ip}")
    public ResponseEntity<FileModel> getReplicationFile(@PathVariable String fileName, @PathVariable String ip) {
        if (!fileName.isEmpty() && !ip.isEmpty()) {
            System.out.println("Running /GetReplicatedFile, name " + fileName + " ownerIP " + ip);
            try {
                nodeService.recieveTCP(ip, fileName);
            } catch (IOException | InterruptedException e) {
                return new ResponseEntity<FileModel>(new FileModel(ip, fileName), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<FileModel>(new FileModel(ip, fileName), HttpStatus.OK);
        }
        return new ResponseEntity<FileModel>(new FileModel(ip, fileName), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/TransferReplicatedFile")
    public ResponseEntity<NodeModel> transferReplicatedFile(@RequestBody FileModel file) {
        if (!file.getFile().isEmpty()) {
            System.out.println("Running /TransferReplicatedFile, name " + file.getFile() + " ownerIP ");
            threadPool.execute(new TCPListner(nodeService, false, file.getFile()));
            return new ResponseEntity<NodeModel>(new NodeModel(file.getFile()), HttpStatus.CREATED);
        }
        return new ResponseEntity<NodeModel>(new NodeModel(file.getFile()), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/HostLocalFile")
    public ResponseEntity<NodeModel> hostLocalFile(@RequestBody FileModel file) {
        if (!file.getFile().isEmpty()) {
            System.out.println("Running /HostLocalFile, name " + file.getFile());
            // Ga thread moete worrexx
            threadPool.execute(new TCPListner(nodeService, true, file.getFile()));
            return new ResponseEntity<NodeModel>(new NodeModel(file.getFile()), HttpStatus.CREATED);
        }
        return new ResponseEntity<NodeModel>(new NodeModel(file.getFile()), HttpStatus.BAD_REQUEST);

    }

    @DeleteMapping("/RemoveReplicatedFile")
    public ResponseEntity<NodeModel> RemoveReplicatedFile(@RequestBody FileModel file) {
        if (!file.getFile().isEmpty()) {
            nodeService.removeReplicatedFile(file.getFile());
            return new ResponseEntity<NodeModel>(new NodeModel(file.getFile()), HttpStatus.CREATED);
        }
        return new ResponseEntity<NodeModel>(new NodeModel(file.getFile()), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/IsHighest")
    public ResponseEntity<NodeModel> isHighest(@RequestBody String value) {
        if (!value.isEmpty()) {
            if (value.equals("true"))
                try {
                    nodeService.setHighest();
                } catch (IOException e) {
                    return new ResponseEntity<NodeModel>(new NodeModel(value), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
            return new ResponseEntity<NodeModel>(new NodeModel(value), HttpStatus.CREATED);
        }
        return new ResponseEntity<NodeModel>(new NodeModel(value), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/IsLowest")
    public ResponseEntity<NodeModel> isLowest(@RequestBody String value) {
        if (!value.isEmpty()) {
            if (value.equals("true"))
                try {
                    nodeService.setLowest();
                } catch (IOException e) {
                    return new ResponseEntity<NodeModel>(new NodeModel(value), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
            return new ResponseEntity<NodeModel>(new NodeModel(value), HttpStatus.CREATED);
        }
        return new ResponseEntity<NodeModel>(new NodeModel(value), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/Kill")
    public ResponseEntity<NodeModel> kill() throws IOException {
        System.out.println("Running /Kill");
        nodeService.shutdown();
        return new ResponseEntity<NodeModel>(new NodeModel(nodeService.name, nodeService.thisIp), HttpStatus.OK);
    }
}