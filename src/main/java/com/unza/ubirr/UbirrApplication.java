package com.unza.ubirr;

import com.unza.ubirr.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class UbirrApplication {

  public static void main(String[] args) {
    SpringApplication.run(UbirrApplication.class, args);
  }
}
