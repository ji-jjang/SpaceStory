package com.juny.spacestory.domain.space.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DetailedSpaceImage {

  private Long id;
  private String imagePath;
}
