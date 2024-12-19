package com.juny.spacestory.domain.reservation.entity;

import com.juny.spacestory.domain.space.entity.DetailedSpace;
import com.juny.spacestory.domain.user.entity.User;
import com.juny.spacestory.global.constant.Constants;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class Reservation {

  private Long id;
  private String status;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Integer guestCount;
  private Integer totalPrice;
  private Long parentId;
  private LocalDateTime createdAt;
  private LocalDateTime deletedAt;

  private DetailedSpace detailedSpace;
  private User user;

  public void cancelReservationByUser() {
    this.status = Constants.RESERVATION_STATUS_CANCEL_PENDING;
    this.deletedAt = LocalDateTime.now();
  }

  public void approveReservationByHost() {
    this.status = Constants.RESERVATION_STATUS_APPROVE;
  }

  public void cancelApprovedReservation() {
    this.status = Constants.RESERVATION_STATUS_CANCEL;
  }

  public void rejectApprovePendingReservationByHost() {
    this.status = Constants.RESERVATION_STATUS_CANCEL;
  }

  public void rejectCancelPendingReservationByHost() {
    this.status = Constants.RESERVATION_STATUS_APPROVE;
    this.deletedAt = null;
  }
}
