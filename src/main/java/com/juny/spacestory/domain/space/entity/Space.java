package com.juny.spacestory.domain.space.entity;

import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Space {

  private Long id;
  private String name;
  private String description;
  private LocalTime openingTime;
  private LocalTime closingTime;
  private Boolean isAdvertised;
  private String verifiedStatus;

  private List<DetailedSpace> detailedSpaces;
}
