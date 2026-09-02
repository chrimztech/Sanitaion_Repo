package com.unza.ubirr;

import com.unza.ubirr.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@EnableScheduling
@EnableAsync
public class UbirrApplication {

  public static void main(String[] args) {
    SpringApplication.run(UbirrApplication.class, args);
  }
}
