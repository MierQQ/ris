package com.nsu.mier.manager.type;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("task")
public class MongoTaskStatus {
    @Id
    @NonNull
    private String uuid;
    private String status;
    private List<String> answer;
    private Instant startTime;

    public MongoTaskStatus() {
        this.answer = new ArrayList<>();
    }

    public MongoTaskStatus(String uuid) {
        this.uuid = uuid;
        this.status = "IN_PROGRESS";
        this.answer = new ArrayList<String>();
        this.startTime = Instant.now();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
}
