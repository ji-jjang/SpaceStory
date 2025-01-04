package com.juny.spacestory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.juny.spacestory.domain.reservation.common.dto.ReqReservationCreate;
import com.juny.spacestory.domain.reservation.common.entity.Reservation;
import com.juny.spacestory.domain.reservation.common.repository.ReservationRepository;
import com.juny.spacestory.domain.reservation.common.service.ReservationPriceCalculateService;
import com.juny.spacestory.domain.reservation.common.service.ReservationService;
import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.slot.repository.PackageSlotPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeSlotPriceRepository;
import com.juny.spacestory.domain.user.common.entity.User;
import com.juny.spacestory.domain.user.common.repository.UserRepository;
import com.juny.spacestory.global.constant.Constants;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

  @InjectMocks private ReservationService reservationService;

  @Mock private TimeSlotPriceRepository timeSlotPriceRepository;
  @Mock private PackageSlotPriceRepository packageSlotPriceRepository;
  @Mock private ReservationPriceCalculateService reservationPriceCalculateService;
  @Mock private ReservationRepository reservationRepository;
  @Mock private UserRepository userRepository;
  @Mock private Clock clock;

  @Test
  @DisplayName("시간제 슬롯으로 예약을 생성한다")
  void createReservationByTimeSlot() {

    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    User user = User.builder().currentPoint(100_000).build();

    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    // given
    LocalDate reservationDate = LocalDate.of(2024, 12, 17);

    ReqReservationCreate reqReservationCreate =
        new ReqReservationCreate(
            Constants.PRICE_TYPE_TIME, reservationDate, Collections.EMPTY_LIST, -1);

    TimeSlotPrice timeSlotPrice1 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 0)).isReserved(false).build();

    TimeSlotPrice timeSlotPrice2 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 30)).isReserved(false).build();

    List<TimeSlotPrice> timeSlotPrices = List.of(timeSlotPrice1, timeSlotPrice2);

    when(timeSlotPriceRepository.findByIdsForUpdateOrderByStartTimeASC(Collections.emptyList()))
        .thenReturn(timeSlotPrices);

    // when
    when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

    Reservation reservation = reservationService.createReservation(reqReservationCreate, -1L, -1L);

    assertThat(reservation).isNotNull();

    assertThat(reservation.getStartDateTime())
        .isEqualTo(LocalDateTime.of(reservationDate, timeSlotPrice1.getStartTime()));

    assertThat(reservation.getEndDateTime())
        .isEqualTo(
            LocalDateTime.of(
                reservationDate,
                timeSlotPrice2.getStartTime().plusMinutes(Constants.TIME_SLOT_INTERVAL)));
  }

  @Test
  @DisplayName("이미 예약된 시간제 슬롯으로 예약을 생성하면 실패한다")
  void createReservationByTimeSlotAndIsReserved() {

    // given
    LocalDate reservationDate = LocalDate.of(2024, 12, 17);

    ReqReservationCreate reqReservationCreate =
        new ReqReservationCreate(
            Constants.PRICE_TYPE_TIME, reservationDate, Collections.EMPTY_LIST, -1);

    TimeSlotPrice timeSlotPrice1 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 0)).isReserved(false).build();

    TimeSlotPrice timeSlotPrice2 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 30)).isReserved(true).build();

    List<TimeSlotPrice> timeSlotPrices = List.of(timeSlotPrice1, timeSlotPrice2);

    when(timeSlotPriceRepository.findByIdsForUpdateOrderByStartTimeASC(Collections.emptyList()))
        .thenReturn(timeSlotPrices);

    // when & then
    assertThatThrownBy(() -> reservationService.createReservation(reqReservationCreate, -1L, -1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("already reserved time slot");
  }

  @Test
  @DisplayName("패키지 슬롯으로 예약을 생성한다")
  void createReservationByPackageSlot() {

    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    // given
    LocalDate reservationDate = LocalDate.of(2024, 12, 17);

    ReqReservationCreate reqReservationCreate =
        new ReqReservationCreate(Constants.PRICE_TYPE_PACKAGE, reservationDate, List.of(-1L), -1);

    PackageSlotPrice packageSlotPrice =
        PackageSlotPrice.builder()
            .id(-1L)
            .name("오전 패키지~!")
            .startTime(LocalDateTime.of(reservationDate, LocalTime.of(8, 0)))
            .endTime(LocalDateTime.of(reservationDate, LocalTime.of(12, 0)))
            .price(40000)
            .isReserved(false)
            .build();

    when(packageSlotPriceRepository.findByIdForUpdate(-1L))
        .thenReturn(Optional.of(packageSlotPrice));

    // when
    Reservation reservation = reservationService.createReservation(reqReservationCreate, -1L, -1L);

    assertThat(reservation).isNotNull();

    assertThat(reservation.getStartDateTime()).isEqualTo(packageSlotPrice.getStartTime());

    assertThat(reservation.getEndDateTime()).isEqualTo(packageSlotPrice.getEndTime());
  }

  @Test
  @DisplayName("이미 예약된 패키지 슬롯으로 예약하면 실패한다")
  void createReservationByPackageSlotAndIsReserved() {

    // given
    LocalDate reservationDate = LocalDate.of(2024, 12, 17);

    ReqReservationCreate reqReservationCreate =
        new ReqReservationCreate(Constants.PRICE_TYPE_PACKAGE, reservationDate, List.of(-1L), -1);

    PackageSlotPrice packageSlotPrice =
        PackageSlotPrice.builder()
            .id(-1L)
            .name("오전 패키지~!")
            .startTime(LocalDateTime.of(reservationDate, LocalTime.of(8, 0)))
            .endTime(LocalDateTime.of(reservationDate, LocalTime.of(12, 0)))
            .price(40000)
            .isReserved(true)
            .build();

    when(packageSlotPriceRepository.findByIdForUpdate(-1L))
        .thenReturn(Optional.of(packageSlotPrice));

    // when
    assertThatThrownBy(() -> reservationService.createReservation(reqReservationCreate, -1L, -1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("already reserved package slot");
  }

  @Test
  @DisplayName("기존 예약을 새로운 예약으로 변경한다. 기존 예약은 승인 -> 취소 대기, 새로운 예약은 승인 대기 상태가 된다")
  void oldReservationUpdateNewReservation() {

    // given
    LocalDate fixedDate = LocalDate.of(2024, 12, 17);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    User user = User.builder().currentPoint(100_000).build();

    Reservation oldReservation =
        Reservation.builder()
            .id(1L)
            .status(Constants.RESERVATION_STATUS_APPROVE)
            .startDateTime(LocalDateTime.of(2024, 12, 19, 8, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 19, 10, 0))
            .guestCount(5)
            .totalPrice(10000)
            .createdAt(LocalDateTime.of(2024, 12, 15, 15, 15, 15))
            .build();

    TimeSlotPrice timeSlotPrice =
        TimeSlotPrice.builder()
            .startTime(LocalTime.of(18, 0))
            .price(15000)
            .isReserved(false)
            .build();

    ReqReservationCreate reqReservationCreate =
        new ReqReservationCreate(
            Constants.PRICE_TYPE_TIME, LocalDate.of(2024, 12, 17), List.of(-1L), 7);

    when(reservationRepository.findById(1L)).thenReturn(Optional.of(oldReservation));
    when(timeSlotPriceRepository.findByIdsForUpdateOrderByStartTimeASC(List.of(-1L)))
        .thenReturn(List.of(timeSlotPrice));

    // when
    when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

    Reservation newReservation =
        reservationService.updateReservationByUser(reqReservationCreate, -1L, 1L);

    // then
    assertThat(oldReservation.getStatus()).isEqualTo(Constants.RESERVATION_STATUS_CANCEL_PENDING);

    assertThat(oldReservation.getParentId()).isNull();

    assertThat(newReservation.getStatus()).isEqualTo(Constants.RESERVATION_STATUS_APPROVE_PENDING);

    assertThat(newReservation.getParentId()).isEqualTo(oldReservation.getId());

    assertThat(newReservation.getStartDateTime())
        .isEqualTo(LocalDateTime.of(fixedDate, timeSlotPrice.getStartTime()));

    assertThat(newReservation.getEndDateTime())
        .isEqualTo(
            LocalDateTime.of(
                fixedDate, timeSlotPrice.getStartTime().plusMinutes(Constants.TIME_SLOT_INTERVAL)));

    assertThat(newReservation.getGuestCount()).isEqualTo(7);

    assertThat(newReservation.getCreatedAt())
        .isEqualTo(LocalDateTime.of(fixedDate, LocalTime.of(0, 0)));
  }

  @Test
  @DisplayName("사용자 예약을 취소한다. 승인 상태에서 취소 대기 상태가 된다")
  void cancelReservationByUserByUser() {

    // given
    Reservation oldReservation =
        Reservation.builder()
            .id(1L)
            .status(Constants.RESERVATION_STATUS_APPROVE)
            .startDateTime(LocalDateTime.of(2024, 12, 19, 8, 0))
            .endDateTime(LocalDateTime.of(2024, 12, 19, 10, 0))
            .guestCount(5)
            .totalPrice(10000)
            .createdAt(LocalDateTime.of(2024, 12, 15, 15, 15, 15))
            .build();

    when(reservationRepository.findById(1L)).thenReturn(Optional.of(oldReservation));

    // when
    reservationService.cancelReservationByUser(1L);

    // then
    assertThat(oldReservation.getStatus()).isEqualTo(Constants.RESERVATION_STATUS_CANCEL_PENDING);
  }

  @Test
  @DisplayName("호스트 예약 변경 승인한다. 새로운 예약 승인 대기에서 승인, 기존 예약 취소 대기에서 취소 상태가 된다")
  void approveUpdateReservationByHost() {

    // given
    Reservation oldReservation =
        Reservation.builder().id(1L).status(Constants.RESERVATION_STATUS_CANCEL_PENDING).build();

    Reservation newReservation =
        Reservation.builder()
            .id(2L)
            .parentId(1L)
            .status(Constants.RESERVATION_STATUS_APPROVE_PENDING)
            .build();

    when(reservationRepository.findById(1L)).thenReturn(Optional.of(oldReservation));
    when(reservationRepository.findById(2L)).thenReturn(Optional.of(newReservation));

    // when
    reservationService.approveUpdateReservationByHost(2L);

    // then
    assertThat(oldReservation.getStatus()).isEqualTo(Constants.RESERVATION_STATUS_CANCEL);

    assertThat(newReservation.getStatus()).isEqualTo(Constants.RESERVATION_STATUS_APPROVE);
  }

  @Test
  @DisplayName("호스트가 예약 변경 거절한다. 새로운 예약 승인 대기에서 거절, 기존 예약 취소 대기에서 승인 상태가 된다.")
  void rejectUpdateReservationByHost() {

    Reservation oldReservation =
        Reservation.builder().id(1L).status(Constants.RESERVATION_STATUS_CANCEL_PENDING).build();

    Reservation newReservation =
        Reservation.builder()
            .id(2L)
            .parentId(1L)
            .status(Constants.RESERVATION_STATUS_APPROVE_PENDING)
            .build();

    when(reservationRepository.findById(1L)).thenReturn(Optional.of(oldReservation));
    when(reservationRepository.findById(2L)).thenReturn(Optional.of(newReservation));

    // when
    reservationService.rejectUpdateReservationByHost(2L);

    // then
    assertThat(oldReservation.getStatus()).isEqualTo(Constants.RESERVATION_STATUS_APPROVE);

    assertThat(newReservation.getStatus()).isEqualTo(Constants.RESERVATION_STATUS_CANCEL);
  }
}
