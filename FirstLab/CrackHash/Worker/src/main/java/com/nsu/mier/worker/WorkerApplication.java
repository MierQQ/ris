package com.nsu.mier.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsync
public class WorkerApplication { public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
