package com.nsu.mier.worker.service;

import com.nsu.mier.worker.xml.CrackHashManagerRequest;
import com.nsu.mier.worker.xml.CrackHashWorkerResponse;
import jakarta.xml.bind.DatatypeConverter;
import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Service
public class WorkerService {

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    private final Duration taskTimeout = Duration.parse("PT5M");

    @Async
    public void crackHashTask(CrackHashManagerRequest body) {
        String alphabet = String.join("", body.getAlphabet().getSymbols());
        int alphabetSize = alphabet.length();
        int positionsNum = body.getMaxLength();
        int partCount = body.getPartCount();
        int partNumber = body.getPartNumber();
        byte[] hexBinary = DatatypeConverter.parseHexBinary(body.getHash());


        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch(Exception e) {
            e.printStackTrace();
        }

        ICombinatoricsVector<String> vector = CombinatoricsFactory.createVector(alphabet.split(""));

        Generator<String> gen = CombinatoricsFactory.createPermutationWithRepetitionGenerator(vector, positionsNum);

        int idx = 0;
        System.out.println("Start");
        Instant startTime = Instant.now();
        for (ICombinatoricsVector<String> perm : gen) {
            if (idx % partCount == partNumber) {
                String str = String.join("", perm.getVector());
                byte[] combHash = md5.digest(str.toString().getBytes());
                if (Arrays.equals(combHash, hexBinary)) {
                    System.out.println("hash: " + str.toString());
                    sendAnswer(body.getRequestId(), str);
                }
            }

            Duration dur = Duration.between(startTime, Instant.now());
            if (dur.toMillis() > taskTimeout.toMillis()) {
                System.out.println("Exceeded time limit: exiting");
                return;
            }

            idx++;

        }
        System.out.println("End");

    }

    private void sendAnswer(String id, String answer) {
        String managerUrl = "http://manager:8080";

        CrackHashWorkerResponse response = new CrackHashWorkerResponse();
        response.setRequestId(id);
        response.setAnswers(new CrackHashWorkerResponse.Answers());
        response.getAnswers().getWords().add(answer);

        restTemplate.patchForObject(managerUrl + "/internal/api/manager/hash/crack/request", response, Void.class);
    }

}
