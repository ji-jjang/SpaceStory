package com.juny.spacestory.domain.reservation.mapper;

import com.juny.spacestory.domain.reservation.dto.ResCreateReservation;
import com.juny.spacestory.domain.reservation.entity.Reservation;

public class ReservationMapper {

  public static ResCreateReservation toResReservation(Reservation reservation) {

    return new ResCreateReservation(
        reservation.getId(),
        reservation.getReservationDate().toString(),
        reservation.getStartDateTime().toString(),
        reservation.getEndDateTime().toString(),
        reservation.getGuestCount(),
        reservation.getGuestCount(),
        reservation.getDetailedSpace().getId(),
        reservation.getUser().getId());
  }
}
