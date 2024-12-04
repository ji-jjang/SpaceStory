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
public class TimeSlotPrice {

  private Long id;
  private LocalTime startTime;
  private Integer price;
  private Boolean isReserved;

  private Reservation reservation;
}