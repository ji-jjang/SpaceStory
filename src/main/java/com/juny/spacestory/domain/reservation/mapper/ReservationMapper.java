package com.juny.spacestory.domain.reservation.mapper;

import com.juny.spacestory.domain.reservation.dto.PageInfo;
import com.juny.spacestory.domain.reservation.dto.ResReservation;
import com.juny.spacestory.domain.reservation.dto.ResReservationList;
import com.juny.spacestory.domain.reservation.dto.SearchCondition;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import java.util.List;

public class ReservationMapper {

  public static ResReservation toResReservation(Reservation reservation) {

    return new ResReservation(
        reservation.getId(),
        reservation.getStatus(),
        reservation.getStartDateTime().toString(),
        reservation.getEndDateTime().toString(),
        reservation.getGuestCount(),
        reservation.getTotalPrice(),
        reservation.getCreatedAt().toString(),
        reservation.getDeletedAt().toString(),
        reservation.getDetailedSpace().getId(),
        reservation.getUser().getId());
  }

  public static ResReservationList toResReservationList(
      List<Reservation> reservations, SearchCondition searchCondition, long totalReservationCount) {

    int totalPages = (int) Math.ceil((double) totalReservationCount / searchCondition.pageSize());

    PageInfo pageInfo =
        new PageInfo(
            searchCondition.pageSize(), searchCondition.page(), totalPages, totalReservationCount);

    List<ResReservation> resReservations =
        reservations.stream().map(ReservationMapper::toResReservation).toList();

    return new ResReservationList(resReservations, pageInfo);
  }
}
