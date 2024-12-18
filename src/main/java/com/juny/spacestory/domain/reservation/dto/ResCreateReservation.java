package com.juny.spacestory.domain.reservation.dto;

public record ResCreateReservation(
    Long id,
    String reservationDate,
    String startDateTime,
    String endDateTime,
    Integer guestCount,
    Integer totalPrice,
    Long detailedSpaceId,
    Long userId) {}
