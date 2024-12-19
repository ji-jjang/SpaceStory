package com.juny.spacestory.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class User {

  private Long id;
  private String email;
  private String password;
  private String name;
  private String role;
}
