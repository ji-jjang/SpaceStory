package com.juny.spacestory.domain.price.entity;

import com.juny.spacestory.domain.reservation.entity.Reservation;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PackageSlotPrice {

  private Long id;
  private String name;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer price;
  private Boolean isReserved;

  private Reservation reservation;
}
