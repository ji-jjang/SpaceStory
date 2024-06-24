package com.juny.spacestory;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;

@RestController
public class TestController {

  @GetMapping("/api/v1/hello")
  public String getHello() {

    return "Get Hello";
  }

  @PostMapping("/api/v1/hello")
  public String postHello() {

    return "Post Hello";
  }

  @PatchMapping("/api/v1/hello")
  public String patchHello() {

    return "Patch Hello";
  }

  @GetMapping("/api/v1/test")
  public String testHello() {

    return "test Hello";
  }
}
