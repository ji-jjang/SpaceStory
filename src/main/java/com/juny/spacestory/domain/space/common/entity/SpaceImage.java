package com.juny.spacestory.domain.space.common.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpaceImage {

  private Long id;
  private String logicalName;
  private String storedName;
  private String extension;
  private Long size;
  private Boolean isRepresent;
  private LocalDateTime createdAt;

  private Space space;
}
