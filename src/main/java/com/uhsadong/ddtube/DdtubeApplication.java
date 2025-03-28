package com.uhsadong.ddtube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DdtubeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdtubeApplication.class, args);
    }

}
