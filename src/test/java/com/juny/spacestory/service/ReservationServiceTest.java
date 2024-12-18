package com.juny.spacestory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.slot.repository.PackageSlotPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeSlotPriceRepository;
import com.juny.spacestory.domain.reservation.dto.ReqCreateReservation;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.repository.ReservationRepository;
import com.juny.spacestory.domain.reservation.service.ReservationPriceCalculateService;
import com.juny.spacestory.domain.reservation.service.ReservationService;
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
  @Mock private Clock clock;

  @Test
  @DisplayName("시간제 슬롯으로 예약을 생성한다")
  void createReservationByTimeSlot() {

    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    // given
    LocalDate reservationDate = LocalDate.of(2024, 12, 17);

    ReqCreateReservation reqCreateReservation =
        new ReqCreateReservation(
            Constants.PRICE_TYPE_TIME, reservationDate, Collections.EMPTY_LIST, -1);

    TimeSlotPrice timeSlotPrice1 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 0)).isReserved(false).build();

    TimeSlotPrice timeSlotPrice2 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 30)).isReserved(false).build();

    List<TimeSlotPrice> timeSlotPrices = List.of(timeSlotPrice1, timeSlotPrice2);

    when(timeSlotPriceRepository.findByIdsForUpdateOrderByStartTimeASC(Collections.emptyList()))
        .thenReturn(timeSlotPrices);

    // when
    Reservation reservation = reservationService.createReservation(reqCreateReservation, -1L, -1L);

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

    ReqCreateReservation reqCreateReservation =
        new ReqCreateReservation(
            Constants.PRICE_TYPE_TIME, reservationDate, Collections.EMPTY_LIST, -1);

    TimeSlotPrice timeSlotPrice1 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 0)).isReserved(false).build();

    TimeSlotPrice timeSlotPrice2 =
        TimeSlotPrice.builder().startTime(LocalTime.of(20, 30)).isReserved(true).build();

    List<TimeSlotPrice> timeSlotPrices = List.of(timeSlotPrice1, timeSlotPrice2);

    when(timeSlotPriceRepository.findByIdsForUpdateOrderByStartTimeASC(Collections.emptyList()))
        .thenReturn(timeSlotPrices);

    // when & then
    assertThatThrownBy(() -> reservationService.createReservation(reqCreateReservation, -1L, -1L))
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

    ReqCreateReservation reqCreateReservation =
        new ReqCreateReservation(Constants.PRICE_TYPE_PACKAGE, reservationDate, List.of(-1L), -1);

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
    Reservation reservation = reservationService.createReservation(reqCreateReservation, -1L, -1L);

    assertThat(reservation).isNotNull();

    assertThat(reservation.getStartDateTime()).isEqualTo(packageSlotPrice.getStartTime());

    assertThat(reservation.getEndDateTime()).isEqualTo(packageSlotPrice.getEndTime());
  }

  @Test
  @DisplayName("이미 예약된 패키지 슬롯으로 예약하면 실패한다")
  void createReservationByPackageSlotAndIsReserved() {

    // given
    LocalDate reservationDate = LocalDate.of(2024, 12, 17);

    ReqCreateReservation reqCreateReservation =
        new ReqCreateReservation(Constants.PRICE_TYPE_PACKAGE, reservationDate, List.of(-1L), -1);

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
    assertThatThrownBy(() -> reservationService.createReservation(reqCreateReservation, -1L, -1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("already reserved package slot");
  }
}
