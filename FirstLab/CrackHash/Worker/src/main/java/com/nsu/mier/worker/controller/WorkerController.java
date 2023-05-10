package com.nsu.mier.worker.controller;


import com.nsu.mier.worker.service.WorkerService;
import com.nsu.mier.worker.xml.CrackHashManagerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class WorkerController {
    private final WorkerService service;

    @Autowired
    public WorkerController(WorkerService service) {
        this.service = service;
    }

    @PostMapping("/api/worker/hash/crack/task")
    public void postMethod(@RequestBody CrackHashManagerRequest body) {
        service.crackHashTask(body);
    }
}
