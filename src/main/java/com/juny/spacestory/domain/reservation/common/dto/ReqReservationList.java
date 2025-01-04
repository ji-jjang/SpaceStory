package com.juny.spacestory.domain.reservation.common.dto;

import java.time.LocalDate;

public record ReqReservationList(
    LocalDate startDate, LocalDate endDate, Integer page, String sort) {

  public ReqReservationList {
    if (startDate == null) {
      startDate = LocalDate.now().minusMonths(3);
    }
    if (endDate == null) {
      endDate = LocalDate.now();
    }
    if (page == null || page <= 0) {
      page = 1;
    }
    if (sort == null || sort.isEmpty()) {
      sort = "created_at:desc";
    }
  }
}
