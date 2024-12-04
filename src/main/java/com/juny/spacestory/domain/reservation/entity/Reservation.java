package com.juny.spacestory.domain.reservation.entity;

import com.juny.spacestory.domain.space.entity.DetailedSpace;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Reservation {

  private Long id;
  private Integer reservationType;
  private LocalDateTime reservationDate;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer guestCount;
  private Integer totalPrice;

  private DetailedSpace detailedSpace;
}
