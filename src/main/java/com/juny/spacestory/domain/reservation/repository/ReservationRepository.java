package com.juny.spacestory.domain.reservation.repository;

import com.juny.spacestory.domain.reservation.entity.Reservation;
import java.time.YearMonth;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationRepository {

  List<Reservation> findAllDetailedSpaceIdAndYearMonths(Long detailedSpaceId,
    List<YearMonth> targetYearMonths);
}
