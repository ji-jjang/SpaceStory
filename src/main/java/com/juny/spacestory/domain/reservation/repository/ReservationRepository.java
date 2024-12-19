package com.juny.spacestory.domain.reservation.repository;

import com.juny.spacestory.domain.reservation.dto.SearchCondition;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationRepository {

  List<Reservation> findAllDetailedSpaceIdAndYearMonths(
      Long detailedSpaceId, List<YearMonth> targetYearMonths);

  void save(Reservation reservation);

  long getTotalReservationCountByUser(SearchCondition searchCondition);

  List<Reservation> findAllReservationsBySearchConditionByUser(SearchCondition searchCondition);

  Optional<Reservation> findById(Long reservationId);

  long getTotalReservationCountByHost(SearchCondition searchCondition);

  List<Reservation> findAllReservationsBySearchConditionByHost(SearchCondition searchCondition);

  long findAllReservationsByAdmin(SearchCondition searchCondition);

  List<Reservation> findAllReservationsBySearchConditionByAdmin(SearchCondition searchCondition);
}
