package com.juny.spacestory.domain.reservation.common.service;

import com.juny.spacestory.domain.point.common.service.PointService;
import com.juny.spacestory.domain.reservation.common.dto.ReqReservationCreate;
import com.juny.spacestory.domain.reservation.common.dto.ReqReservationList;
import com.juny.spacestory.domain.reservation.common.dto.ReqReservationUpdate;
import com.juny.spacestory.domain.reservation.common.dto.SearchCondition;
import com.juny.spacestory.domain.reservation.common.entity.Reservation;
import com.juny.spacestory.domain.reservation.common.repository.ReservationRepository;
import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.slot.repository.PackageSlotPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeSlotPriceRepository;
import com.juny.spacestory.domain.space.common.entity.DetailedSpace;
import com.juny.spacestory.domain.space.common.entity.Space;
import com.juny.spacestory.domain.space.common.repository.SpaceRepository;
import com.juny.spacestory.domain.user.common.entity.User;
import com.juny.spacestory.domain.user.common.repository.UserRepository;
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

  private final PointService pointService;

  private final Clock clock;

  private static void rejectCancelPendingReservationByHost(Reservation oldReservation) {

    if (!oldReservation.getStatus().equals(Constants.RESERVATION_STATUS_CANCEL_PENDING)) {

      throw new RuntimeException(
          "Pending cancel reservation can only be rejected when its status is CANCEL_PENDING");
    }
    oldReservation.rejectCancelPendingReservationByHost();
  }

  private static void rejectApprovePendingReservationByHost(Reservation newReservation) {

    if (!newReservation.getStatus().equals(Constants.RESERVATION_STATUS_APPROVE_PENDING)) {

      throw new RuntimeException(
          "Pending approve reservation can only be rejected when its status is APPROVE_PENDING");
    }

    newReservation.rejectApprovePendingReservationByHost();
  }

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
      ReqReservationCreate req, Long detailedSpaceId, Long userId) {

    User user = getUser(userId);

    if (req.reservationType().equals(Constants.PRICE_TYPE_TIME)) {

      Reservation timeReservation = createTimeReservation(req, detailedSpaceId, userId);

      processPointPayment(timeReservation, user);

      return timeReservation;
    }

    Reservation packageReservation = createPackageReservation(req, detailedSpaceId, userId);

    processPointPayment(packageReservation, user);

    return packageReservation;
  }

  private void processPointPayment(Reservation packageReservation, User user) {
    int totalPrice = packageReservation.getTotalPrice();

    if (user.getCurrentPoint() < totalPrice) {
      throw new RuntimeException(
          String.format(
              "user not enough point: %d, payAmount: %d", user.getCurrentPoint(), totalPrice));
    }

    pointService.processPointPayment(
        -packageReservation.getTotalPrice(),
        user,
        packageReservation.getDetailedSpace().getSpace().getUser().getId(),
        Constants.POINT_SPACE_RESERVATION_REASON);
  }

  /**
   *
   *
   * <h1>예약 단건 조회 (관리자) </h1>
   *
   * @param reservationId 예약 아이디
   * @return 예약
   */
  public Reservation getReservationByReservationIdByHost(Long reservationId) {

    return getReservation(reservationId);
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
   * <h1>예약 단건 조회 (사용자, 호스트 사용) </h1>
   *
   * @param detailedSpaceId 상세공간 ID
   * @param reservationId 예약 ID
   * @param userId 유저 ID
   * @return Reservation
   */
  public Reservation getReservationByReservationIdByUser(
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

  /**
   *
   *
   * <h1>기존 승인된 예약을 새로운 예약으로 변경</h1>
   *
   * - 기존 예약 상태는 취소 대기, 새로운 예약 상태는 승인 대기
   *
   * @param req ReqReservationCreate
   * @param detailedSpaceId 상세공간 ID
   * @param reservationId 예약 ID
   * @return 새로운 예약
   */
  @Transactional
  public Reservation updateReservationByUser(
      ReqReservationCreate req, Long detailedSpaceId, Long reservationId) {

    Reservation oldReservation = getReservation(reservationId);

    if (!oldReservation.getStatus().equals(Constants.RESERVATION_STATUS_APPROVE)) {

      throw new RuntimeException("reservation can only be updated when its status is APPROVED");
    }

    oldReservation.cancelReservationByUser();

    reservationRepository.updateReservation(
        ReqReservationUpdate.builder()
            .id(reservationId)
            .status(oldReservation.getStatus())
            .deletedAt(oldReservation.getDeletedAt())
            .build());

    Reservation newReservation = createReservation(req, detailedSpaceId, reservationId);

    return newReservation.toBuilder().parentId(reservationId).build();
  }

  /**
   *
   *
   * <h1>사용자 예약 취소 요청 </h1>
   *
   * <br>
   * - 예약 승인 -> 취소 대기 상태로 변경
   *
   * @param reservationId 예약 아이디
   * @return 변경된 예약
   */
  @Transactional
  public Reservation cancelReservationByUser(Long reservationId) {

    Reservation reservation = getReservation(reservationId);

    if (!reservation.getStatus().equals(Constants.RESERVATION_STATUS_APPROVE)) {
      throw new RuntimeException("reservation can only be canceled when its status is APPROVED");
    }

    reservation.cancelReservationByUser();

    reservationRepository.updateReservation(
        ReqReservationUpdate.builder()
            .id(reservationId)
            .status(reservation.getStatus())
            .deletedAt(reservation.getDeletedAt())
            .build());

    return reservation;
  }

  /**
   *
   *
   * <h1>호스트 예약 변경 승인 </h1>
   *
   * <br>
   * - 새로운 예약 승인 대기에서 승인, 기존 예약 취소 대기에서 취소
   *
   * @param reservationId 예약 아이디
   * @return 새로운 예약 상태
   */
  @Transactional
  public Reservation approveUpdateReservationByHost(Long reservationId) {

    Reservation newReservation = getReservation(reservationId);

    approveUpdateReservationByHost(getReservation(reservationId));

    cancelUpdateReservationByHost(getReservation(newReservation.getParentId()));

    return newReservation;
  }

  /**
   *
   *
   * <h1>호스트 예약 변경 거절</h1>
   *
   * - 새로운 예약 승인 대기에서 거절, 기존 예약 취소 대기에서 승인
   *
   * @param reservationId 예약 아이디
   * @return 새로운 예약 상태
   */
  @Transactional
  public Reservation rejectUpdateReservationByHost(Long reservationId) {

    Reservation newReservation = getReservation(reservationId);

    rejectApprovePendingReservationByHost(newReservation);

    rejectCancelPendingReservationByHost(getReservation(newReservation.getParentId()));

    return newReservation;
  }

  private Reservation createTimeReservation(
      ReqReservationCreate req, Long detailedSpaceId, Long userId) {

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
            .status(Constants.RESERVATION_STATUS_APPROVE_PENDING)
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
            .createdAt(LocalDateTime.now(clock))
            .detailedSpace(DetailedSpace.builder().id(detailedSpaceId).build())
            .user(User.builder().id(userId).build())
            .build();

    reservationRepository.save(reservation);

    return reservation;
  }

  private Reservation createPackageReservation(
      ReqReservationCreate req, Long detailedSpaceId, Long userId) {

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
            .status(Constants.RESERVATION_STATUS_APPROVE_PENDING)
            .startDateTime(packageSlot.getStartTime())
            .endDateTime(packageSlot.getEndTime())
            .guestCount(req.guestCount())
            .totalPrice(totalPrice)
            .createdAt(LocalDateTime.now(clock))
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

  private void cancelUpdateReservationByHost(Reservation oldReservation) {

    if (!oldReservation.getStatus().equals(Constants.RESERVATION_STATUS_CANCEL_PENDING)) {

      throw new RuntimeException(
          "reservation can only be cancelled when its status is CANCEL_PENDING");
    }

    oldReservation.cancelApprovedReservation();

    reservationRepository.cancelReservation(oldReservation.getId());
  }

  private void approveUpdateReservationByHost(Reservation newReservation) {

    if (!newReservation.getStatus().equals(Constants.RESERVATION_STATUS_APPROVE_PENDING)) {

      throw new RuntimeException(
          "reservation can only be approved when its status is APPROVE_PENDING");
    }

    newReservation.approveReservationByHost();

    reservationRepository.approveReservation(newReservation.getId());
  }
}
