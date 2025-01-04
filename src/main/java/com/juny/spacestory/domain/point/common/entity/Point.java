package com.juny.spacestory.domain.point.common.entity;

import com.juny.spacestory.domain.user.common.entity.User;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public class Point {

  private Long id;
  private Integer amount;
  private String reason;
  private LocalDateTime createdAt;
  private Long hostID;

  private User user;
}
