package com.juny.spacestory.domain.reservation.mapper;

import com.juny.spacestory.domain.reservation.dto.PageInfo;
import com.juny.spacestory.domain.reservation.dto.ResCreateReservation;
import com.juny.spacestory.domain.reservation.dto.ResReservationList;
import com.juny.spacestory.domain.reservation.dto.SearchCondition;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import java.util.List;

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

  public static ResReservationList toResReservationList(
      List<Reservation> reservations, SearchCondition searchCondition, long totalReservationCount) {

    int totalPages = (int) Math.ceil((double) totalReservationCount / searchCondition.pageSize());

    PageInfo pageInfo =
        new PageInfo(
            searchCondition.pageSize(), searchCondition.page(), totalPages, totalReservationCount);

    List<ResCreateReservation> resReservations =
        reservations.stream().map(ReservationMapper::toResReservation).toList();

    return new ResReservationList(resReservations, pageInfo);
  }
}
