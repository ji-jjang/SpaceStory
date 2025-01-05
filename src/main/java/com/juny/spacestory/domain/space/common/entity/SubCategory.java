package com.juny.spacestory.domain.space.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategory {

  private Long id;
  private String name;

  private MainCategory mainCategory;
}
