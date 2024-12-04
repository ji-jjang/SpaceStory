package com.juny.spacestory.domain.reservation.entity;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PriceInfo {

  private Integer priceType;
  private Integer dayOfWeek;
  private LocalTime startTime;
  private LocalTime endTime;
  private String name;
  private Integer price;
}
