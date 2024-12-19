package com.juny.spacestory.domain.reservation.service;

import com.juny.spacestory.domain.reservation.dto.ReqReservationList;
import com.juny.spacestory.domain.reservation.dto.SearchCondition;
import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.slot.repository.PackageSlotPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeSlotPriceRepository;
import com.juny.spacestory.domain.reservation.dto.ReqCreateReservation;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.repository.ReservationRepository;
import com.juny.spacestory.domain.space.entity.DetailedSpace;
import com.juny.spacestory.domain.space.entity.Space;
import com.juny.spacestory.domain.space.repository.SpaceRepository;
import com.juny.spacestory.domain.user.entity.User;
import com.juny.spacestory.domain.user.repository.UserRepository;
import com.juny.spacestory.global.constant.Constants;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

  private final TimeSlotPriceRepository timeSlotPriceRepository;

  private final PackageSlotPriceRepository packageSlotPriceRepository;

  private final ReservationPriceCalculateService reservationPriceCalculateService;

  private final ReservationRepository reservationRepository;

  private final SpaceRepository spaceRepository;

  private final UserRepository userRepository;

  private final Clock clock;

  /**
   *
   *
   * <h1>예약 생성 </h1>
   *
   * @param req ReqCreateReservation
   * @param detailedSpaceId 상세공간 ID
   * @param userId 유저 ID
   * @return Reservation
   */
  @Transactional
  public Reservation createReservation(
      ReqCreateReservation req, Long detailedSpaceId, Long userId) {

    if (req.reservationType().equals(Constants.PRICE_TYPE_TIME)) {

      return createTimeReservation(req, detailedSpaceId, userId);
    }

    return createPackageReservation(req, detailedSpaceId, userId);
  }

  /**
   *
   *
   * <h1>예약 목록 조회 시 공통 검색 조건 생성 (사용자, 호스트, 관리자) </h1>
   *
   * @param req ReqReservationList
   * @param userId userId
   * @param isHost isHost
   * @return SearchCondition
   */
  public SearchCondition getReservationListSearchCondition(
      ReqReservationList req, Long userId, boolean isHost) {

    int page = req.page() - 1;
    int pageSize = Constants.DEFAULT_RESERVATION_PAGE_SIZE;
    int offset = page * pageSize;
    String[] sort = req.sort().split(":");
    List<Long> detailedSpaceIds = Collections.emptyList();

    if (isHost) {
      detailedSpaceIds = spaceRepository.findAllDetailedSpaceIdsByHostId(userId);
    }

    return new SearchCondition(
        req.startDate().toString() + " 00:00:00",
        req.endDate().toString() + " 23:59:59",
        page,
        pageSize,
        offset,
        sort[0],
        sort[1],
        userId,
        detailedSpaceIds);
  }

  /**
   *
   *
   * <h1>사용자 예약 목록 조회, 예약 전체 개수 DB 쿼리 </h1>
   *
   * @param searchCondition 검색 조건
   * @return 총 예약 개수
   */
  public long getTotalReservationCountByUser(SearchCondition searchCondition) {

    return reservationRepository.getTotalReservationCountByUser(searchCondition);
  }

  /**
   *
   *
   * <h1>사용자 예약 목록 조회, 예약 목록 DB 쿼리 </h1>
   *
   * @param searchCondition 검색 조건
   * @return 예약 목록
   */
  public List<Reservation> getReservationListByUser(SearchCondition searchCondition) {

    return reservationRepository.findAllReservationsBySearchConditionByUser(searchCondition);
  }

  /**
   *
   *
   * <h1>호스트 예약 목록 조회, 예약 전체 개수 DB 쿼리 </h1>
   *
   * @param searchCondition 검색 조건
   * @return 총 예약 개수
   */
  public long getTotalReservationCountByHost(SearchCondition searchCondition) {

    return reservationRepository.getTotalReservationCountByHost(searchCondition);
  }

  /**
   *
   *
   * <h1>호스트 예약 목록 조회, 예약 목록 DB 쿼리 </h1>
   *
   * @param searchCondition 검색 조건
   * @return 예약 목록
   */
  public List<Reservation> getReservationListByHost(SearchCondition searchCondition) {

    return reservationRepository.findAllReservationsBySearchConditionByHost(searchCondition);
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
   * <h1>예약 단건 조회 (사용자, 호스트, 관리자 사용) </h1>
   *
   * @param detailedSpaceId 상세공간 ID
   * @param reservationId 예약 ID
   * @param userId 유저 ID
   * @return Reservation
   */
  public Reservation getReservationByReservationId(
      Long detailedSpaceId, Long reservationId, Long userId) {

    Reservation reservation = getReservation(reservationId);

    User user = getUser(userId);

    String role = user.getRole();

    if (role.equals(Constants.ROLE_USER) && reservation.getUser().getId().equals(userId)) {
      throw new RuntimeException("user can't view this reservation, id not match");
    }

    if (role.equals(Constants.ROLE_HOST)) {

      Space space =
          spaceRepository
              .findByDetailedSpaceId(detailedSpaceId)
              .orElseThrow(() -> new RuntimeException("detailed space id is invalid"));

      if (!space.getUser().getId().equals(userId)) {
        throw new RuntimeException("host user can't view this reservation, id not match");
      }
    }

    return reservation;
  }

  private Reservation createTimeReservation(
      ReqCreateReservation req, Long detailedSpaceId, Long userId) {

    List<TimeSlotPrice> timeSlotPrices =
        timeSlotPriceRepository.findByIdsForUpdateOrderByStartTimeASC(req.slotIds());

    if (timeSlotPrices.isEmpty()) {
      throw new RuntimeException("invalid time slots id");
    }

    for (var timeSlot : timeSlotPrices) {
      if (timeSlot.getIsReserved()) {
        throw new RuntimeException("already reserved time slot");
      }
    }

    timeSlotPriceRepository.updateIsReservedByIds(req.slotIds());

    int totalPrice =
        reservationPriceCalculateService.calculateTotalPrice(
            req.reservationType(), req.guestCount(), timeSlotPrices, null, detailedSpaceId);

    Reservation reservation =
        Reservation.builder()
            .reservationDate(LocalDateTime.now(clock))
            .startDateTime(
                LocalDateTime.of(req.reservationDate(), timeSlotPrices.getFirst().getStartTime()))
            .endDateTime(
                LocalDateTime.of(
                    req.reservationDate(),
                    timeSlotPrices
                        .getLast()
                        .getStartTime()
                        .plusMinutes(Constants.TIME_SLOT_INTERVAL)))
            .guestCount(req.guestCount())
            .totalPrice(totalPrice)
            .detailedSpace(DetailedSpace.builder().id(detailedSpaceId).build())
            .user(User.builder().id(userId).build())
            .build();

    reservationRepository.save(reservation);

    return reservation;
  }

  private Reservation createPackageReservation(
      ReqCreateReservation req, Long detailedSpaceId, Long userId) {

    PackageSlotPrice packageSlot =
        packageSlotPriceRepository
            .findByIdForUpdate(req.slotIds().getFirst())
            .orElseThrow(() -> new RuntimeException("invalid package slot id"));

    if (packageSlot.getIsReserved()) {
      throw new RuntimeException("already reserved package slot");
    }

    packageSlotPriceRepository.updateIsReservedById(req.slotIds().getFirst());

    int totalPrice =
        reservationPriceCalculateService.calculateTotalPrice(
            req.reservationType(), req.guestCount(), null, packageSlot, detailedSpaceId);

    Reservation reservation =
        Reservation.builder()
            .reservationDate(LocalDateTime.now(clock))
            .startDateTime(packageSlot.getStartTime())
            .endDateTime(packageSlot.getEndTime())
            .guestCount(req.guestCount())
            .totalPrice(totalPrice)
            .detailedSpace(DetailedSpace.builder().id(detailedSpaceId).build())
            .user(User.builder().id(userId).build())
            .build();

    reservationRepository.save(reservation);

    return reservation;
  }

  private Reservation getReservation(Long reservationId) {

    return reservationRepository
        .findById(reservationId)
        .orElseThrow(() -> new RuntimeException("reservation id invalid"));
  }

  private User getUser(Long userId) {

    return userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("user not found"));
  }
}
