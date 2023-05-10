package com.nsu.mier.manager.service;

import com.nsu.mier.manager.json.HashAndLength;
import com.nsu.mier.manager.json.RequestId;
import com.nsu.mier.manager.json.TaskStatus;
import com.nsu.mier.manager.xml.CrackHashManagerRequest;
import com.nsu.mier.manager.xml.CrackHashWorkerResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ManagerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ConcurrentHashMap<RequestId, TaskStatus> idAndStatus;
    private static final String LETTERS_AND_DIGITS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private final Duration taskTimeout = Duration.parse("PT5M");

    public ManagerService() {
        this.idAndStatus = new ConcurrentHashMap<>();
    }

    private CrackHashManagerRequest.Alphabet initAlphabet() {
        CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();

        for (String charString : LETTERS_AND_DIGITS.split("")) {
            alphabet.getSymbols().add(charString);
        }

        return alphabet;
    }

    public RequestId getRequestId(HashAndLength body) {

        String address = "balancer";

        RequestId requestId = new RequestId(UUID.randomUUID().toString());
        idAndStatus.put(requestId, new TaskStatus());


        CrackHashManagerRequest request = new CrackHashManagerRequest();

        request.setRequestId(requestId.getRequestId());
        request.setHash(body.getHash());
        request.setMaxLength(body.getMaxLength());
        request.setAlphabet(initAlphabet());

        int partCount = Integer.parseInt(System.getenv("COUNT_OF_WORKERS"));
        request.setPartCount(partCount);

        for (int part = 0; part < partCount; part++) {
            String workerUrl = "http://" + address + ":8082";
            request.setPartNumber(part);
            restTemplate.postForObject(workerUrl + "/internal/api/worker/hash/crack/task", request, Void.class);
        }

        return requestId;
    }

    public TaskStatus getTaskStatus(RequestId id) {
        TaskStatus status = idAndStatus.get(id);
        System.out.println(status.toString());

        Duration dur = Duration.between(status.getStartTime(), Instant.now());
        if (dur.toMillis() > taskTimeout.toMillis() && status.getAnswer().isEmpty()) {
            status.setStatus("TIMEOUT");
        }

        return status;
    }

    public void receiveAnswer(CrackHashWorkerResponse response) {
        if (response.getAnswers().getWords().isEmpty()) {
            return;
        }

        TaskStatus status = idAndStatus.get(new RequestId(response.getRequestId()));
        if (status == null) {
            return;
        }
        System.out.println(status);

        status.getAnswer().addAll(response.getAnswers().getWords());
        status.setStatus("READY");
    }

}
