package com.lanahra.whitelist;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitService {

    @RabbitListener(queues = "insertion.queue")
    public void listen(String data) {
        System.out.println(data);
    }
}
