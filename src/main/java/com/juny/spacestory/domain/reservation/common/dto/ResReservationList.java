package com.juny.spacestory.domain.reservation.common.dto;

import java.util.List;

public record ResReservationList(List<ResReservation> reservationList, PageInfo pageInfo) {}
