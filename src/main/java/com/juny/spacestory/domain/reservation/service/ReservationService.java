package com.juny.spacestory.domain.reservation.service;

import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.slot.repository.PackageSlotPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeSlotPriceRepository;
import com.juny.spacestory.domain.reservation.dto.ReqCreateReservation;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.repository.ReservationRepository;
import com.juny.spacestory.domain.space.entity.DetailedSpace;
import com.juny.spacestory.domain.user.entity.User;
import com.juny.spacestory.global.constant.Constants;
import java.time.Clock;
import java.time.LocalDateTime;
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
}
