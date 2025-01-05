package com.juny.spacestory.domain.space.common.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainCategory {

  private Long id;
  private String name;

  private List<SubCategory> subCategories;
}
