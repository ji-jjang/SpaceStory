package com.juny.spacestory.domain.reservation.dto;

public record ResReservation(
    Long id,
    String status,
    String startDateTime,
    String endDateTime,
    Integer guestCount,
    Integer totalPrice,
    String createdAt,
    String deletedAt,
    Long detailedSpaceId,
    Long userId) {}
