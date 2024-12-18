package com.juny.spacestory.domain.reservation.dto;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

public record ReqCreateReservation(
    @Pattern(regexp = "time|package", message = "reservation type time or package")
        String reservationType,
    LocalDate reservationDate,
    List<Long> slotIds,
    Integer guestCount) {}
