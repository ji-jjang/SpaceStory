package com.juny.spacestory.domain.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReservationInfo {

  private Long id;
  private ReservationType type;
  private Integer minimalCapacity;
  private Integer maximalCapacity;
  private Integer spaceHourlyRate;
  private Integer personHourlyRate;
  private Integer standardCapacity;
  private Integer perPersonAdditionalRate;
}
