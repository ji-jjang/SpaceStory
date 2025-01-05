package com.juny.spacestory.domain.space.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpaceSubCategory {

  private Long spaceId;
  private Long subCategoryId;
}
