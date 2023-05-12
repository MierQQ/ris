package com.nsu.mier.worker.controller;


import com.nsu.mier.worker.service.WorkerService;
import com.nsu.mier.worker.xml.CrackHashManagerRequest;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RabbitListener(queues = "${rabbitmq.request.queue}", id = "worker")
public class WorkerController {
    @PreDestroy
    public void destroy() throws IOException {
        System.out.println("Rejecting tasks");
        List<Long> keyValues = Collections.list(tagChanelMap.keys());
        for (var it : keyValues) {
            tagChanelMap.get(it).basicReject(it, true);
        }
    }

    private ConcurrentHashMap<Long, Channel> tagChanelMap = new ConcurrentHashMap<>();

    private final WorkerService service;

    @Autowired
    public WorkerController(WorkerService service) {
        this.service = service;
    }

    @RabbitHandler
    public void postMethod(@RequestBody CrackHashManagerRequest body, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        tagChanelMap.put(tag, channel);
        try {
            service.crackHashTask(body, channel, tag, tagChanelMap);
        } catch (Exception e) {
            System.out.println("Reject " + body.getPartNumber());
            tagChanelMap.remove(tag);
            channel.basicReject(tag, true);
            throw e;
        }
        tagChanelMap.remove(tag);
        channel.basicAck(tag, false);
    }
}
