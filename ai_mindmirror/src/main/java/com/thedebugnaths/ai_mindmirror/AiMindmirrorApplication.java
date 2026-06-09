package com.thedebugnaths.ai_mindmirror;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiMindmirrorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMindmirrorApplication.class, args);
    }

}
