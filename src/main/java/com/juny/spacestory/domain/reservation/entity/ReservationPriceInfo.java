package com.juny.spacestory.domain.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReservationPriceInfo {

  private Long id;
  private String type;
  private Integer standardCapacity;
  private Integer perPersonAdditionalRate;
  private Boolean isPerPersonRate;
}
