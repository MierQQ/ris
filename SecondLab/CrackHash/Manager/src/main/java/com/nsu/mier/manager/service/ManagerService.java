package com.nsu.mier.manager.service;

import com.nsu.mier.manager.json.HashAndLength;
import com.nsu.mier.manager.json.RequestId;
import com.nsu.mier.manager.json.TaskStatus;
import com.nsu.mier.manager.repository.TaskRepository;
import com.nsu.mier.manager.type.MongoTaskStatus;
import com.nsu.mier.manager.xml.CrackHashManagerRequest;
import com.nsu.mier.manager.xml.CrackHashWorkerResponse;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManagerService {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private final Duration taskTimeout = Duration.parse("PT5M");

    private AmqpTemplate rabbitTemplate;
    private Queue requestQueue;

    private TaskRepository repository;

    @Autowired
    public ManagerService(AmqpTemplate rabbitTemplate, Queue requestQueue, TaskRepository repository) {
        this.rabbitTemplate = rabbitTemplate;
        this.requestQueue = requestQueue;
        this.repository = repository;
    }

    public RequestId getRequestId(HashAndLength body) {
        CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();

        for (String charString : ALPHABET.split("")) {
            alphabet.getSymbols().add(charString);
        }

        RequestId requestId = new RequestId(UUID.randomUUID().toString());
        repository.save(new MongoTaskStatus(requestId.getRequestId()));


        CrackHashManagerRequest request = new CrackHashManagerRequest();

        request.setRequestId(requestId.getRequestId());
        request.setHash(body.getHash());
        request.setMaxLength(body.getMaxLength());
        request.setAlphabet(alphabet);

        int partCount = Integer.parseInt(System.getenv("COUNT_OF_WORKERS"));
        request.setPartCount(partCount);

        for (int part = 0; part < partCount; part++) {
            request.setPartNumber(part);

            rabbitTemplate.convertAndSend(requestQueue.getName(), request);
        }

        return requestId;
    }

    public ResponseEntity<TaskStatus> getTaskStatus(RequestId id) {
        Optional<MongoTaskStatus> res = repository.findById(id.getRequestId());
        if (res.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        TaskStatus status = new TaskStatus();
        status.setStatus(res.get().getStatus());
        status.setStartTime(res.get().getStartTime());
        status.setAnswer(res.get().getAnswer());

        Duration dur = Duration.between(status.getStartTime(), Instant.now());
        if (dur.toMillis() > taskTimeout.toMillis() && status.getAnswer().isEmpty()) {
            status.setStatus("TIMEOUT");
        }

        return ResponseEntity.ok(status);
    }

    public void receiveAnswer(CrackHashWorkerResponse response) {
        if (response.getAnswers().getWords().isEmpty()) {
            return;
        }
        Optional<MongoTaskStatus> res = repository.findById(response.getRequestId());
        if (res.isEmpty()){
            return;
        }

        MongoTaskStatus status = res.get();
        for (var it : response.getAnswers().getWords()) {
            if (!status.getAnswer().contains(it)) {
                status.getAnswer().add(it);
            }
        }

        status.setStatus("READY");
        repository.save(status);
    }

}
