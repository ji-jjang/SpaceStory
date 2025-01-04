package com.juny.spacestory.domain.reservation.admin.service;

import com.juny.spacestory.domain.reservation.common.dto.SearchCondition;
import com.juny.spacestory.domain.reservation.common.entity.Reservation;
import com.juny.spacestory.domain.reservation.common.repository.ReservationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReservationService {

  private final ReservationRepository reservationRepository;

  /**
   *
   *
   * <h1>관리자 예약 목록 조회, 예약 목록 DB 쿼리 </h1>
   *
   * @param searchCondition 검색 조건
   * @return 예약 목록
   */
  public List<Reservation> getReservationListByAdmin(SearchCondition searchCondition) {

    return reservationRepository.findAllReservationsBySearchConditionByAdmin(searchCondition);
  }

  /**
   *
   *
   * <h1>관리자 예약 목록 조회, 예약 전체 개수 DB 쿼리 </h1>
   *
   * @param searchCondition 검색 조건
   * @return 총 예약 개수
   */
  public long getTotalReservationCountByAdmin(SearchCondition searchCondition) {

    return reservationRepository.findAllReservationsByAdmin(searchCondition);
  }

  private Reservation getReservation(Long reservationId) {

    return reservationRepository
        .findById(reservationId)
        .orElseThrow(() -> new RuntimeException("reservation id invalid"));
  }
}
