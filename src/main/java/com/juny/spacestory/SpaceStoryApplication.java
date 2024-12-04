package com.juny.spacestory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
  servers = {
    @Server(url = "https://spacestory.duckdns.org"),
    @Server(url = "http://localhost:8080")
  })
@SpringBootApplication
public class SpaceStoryApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpaceStoryApplication.class, args);
  }

}
