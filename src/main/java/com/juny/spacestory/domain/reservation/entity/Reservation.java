package com.juny.spacestory.domain.reservation.entity;

import com.juny.spacestory.domain.space.entity.DetailedSpace;
import com.juny.spacestory.domain.user.entity.User;
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
  private LocalDateTime reservationDate;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Integer guestCount;
  private Integer totalPrice;

  private DetailedSpace detailedSpace;
  private User user;
}
