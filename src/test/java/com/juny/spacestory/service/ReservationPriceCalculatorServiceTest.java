package com.juny.spacestory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.reservation.entity.ReservationPriceInfo;
import com.juny.spacestory.domain.reservation.repository.ReservationPriceInfoRepository;
import com.juny.spacestory.domain.reservation.service.ReservationPriceCalculateService;
import com.juny.spacestory.global.constant.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReservationPriceCalculatorServiceTest {

  @InjectMocks private ReservationPriceCalculateService reservationPriceCalculateService;

  @Mock private ReservationPriceInfoRepository reservationPriceInfoRepository;

  @Test
  @DisplayName("시간제 요금 계산, 기본 슬롯 요금 4000, 추가 인원 0명, 한명 당 추가요금 3000이면 총 4000원")
  void calculateTimeSlotPrice() {

    // given
    int guestCount = 5;
    List<TimeSlotPrice> timeSlotPrices = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      TimeSlotPrice timeSlot = TimeSlotPrice.builder().price(1000).build();
      timeSlotPrices.add(timeSlot);
    }

    int standardCapacity = 5;
    ReservationPriceInfo reservationPriceInfo =
        ReservationPriceInfo.builder()
            .type(Constants.PRICE_TYPE_TIME)
            .standardCapacity(standardCapacity)
            .perPersonAdditionalRate(3000)
            .isPerPersonRate(false)
            .build();

    when(reservationPriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            -1L, Constants.PRICE_TYPE_TIME))
        .thenReturn(Optional.of(reservationPriceInfo));

    // when
    int totalPrice =
        reservationPriceCalculateService.calculateTotalPrice(
            Constants.PRICE_TYPE_TIME, guestCount, timeSlotPrices, null, -1L);

    // then
    assertThat(totalPrice).isEqualTo(4000);
  }

  @Test
  @DisplayName("시간제 요금 계산, 기본 슬롯 요금 4000, 5명 추가 인원, 한명 당 추가요금 3000이면 총 19000원")
  void calculateTimeSlotPriceAdditionalFare() {

    // given
    int guestCount = 10;
    List<TimeSlotPrice> timeSlotPrices = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      TimeSlotPrice timeSlot = TimeSlotPrice.builder().price(1000).build();
      timeSlotPrices.add(timeSlot);
    }

    int standardCapacity = 5;
    ReservationPriceInfo reservationPriceInfo =
        ReservationPriceInfo.builder()
            .type(Constants.PRICE_TYPE_TIME)
            .standardCapacity(standardCapacity)
            .perPersonAdditionalRate(3000)
            .isPerPersonRate(false)
            .build();

    when(reservationPriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            -1L, Constants.PRICE_TYPE_TIME))
        .thenReturn(Optional.of(reservationPriceInfo));

    // when
    int totalPrice =
        reservationPriceCalculateService.calculateTotalPrice(
            Constants.PRICE_TYPE_TIME, guestCount, timeSlotPrices, null, -1L);

    // then
    assertThat(totalPrice).isEqualTo(19000);
  }

  @Test
  @DisplayName("시간제 요금 계산, 기본 슬롯 요금 4000, 총 10명, 사람 당 요금제면 총 40000원")
  void calculateTimeSlotPricePerPersonFare() {

    // given
    int guestCount = 10;
    List<TimeSlotPrice> timeSlotPrices = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      TimeSlotPrice timeSlot = TimeSlotPrice.builder().price(1000).build();
      timeSlotPrices.add(timeSlot);
    }

    int standardCapacity = 5;
    ReservationPriceInfo reservationPriceInfo =
        ReservationPriceInfo.builder()
            .type(Constants.PRICE_TYPE_TIME)
            .standardCapacity(standardCapacity)
            .perPersonAdditionalRate(3000)
            .isPerPersonRate(true)
            .build();

    when(reservationPriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            -1L, Constants.PRICE_TYPE_TIME))
        .thenReturn(Optional.of(reservationPriceInfo));

    // when
    int totalPrice =
        reservationPriceCalculateService.calculateTotalPrice(
            Constants.PRICE_TYPE_TIME, guestCount, timeSlotPrices, null, -1L);

    // then
    assertThat(totalPrice).isEqualTo(40000);
  }

  @Test
  @DisplayName("패키지 요금 계산, 기본 요금 5000, 추가 인원 0명, 한명 당 추가요금 3000이면 총 요금 5000원")
  void calculatePackageSlotPrice() {

    // given
    int guestCount = 5;
    PackageSlotPrice packageSlotPrice = PackageSlotPrice.builder().price(5000).build();

    int standardCapacity = 5;
    ReservationPriceInfo reservationPriceInfo =
        ReservationPriceInfo.builder()
            .type(Constants.PRICE_TYPE_PACKAGE)
            .standardCapacity(standardCapacity)
            .perPersonAdditionalRate(3000)
            .isPerPersonRate(true)
            .build();

    when(reservationPriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            -1L, Constants.PRICE_TYPE_PACKAGE))
        .thenReturn(Optional.of(reservationPriceInfo));

    // when
    int totalPrice =
        reservationPriceCalculateService.calculateTotalPrice(
            Constants.PRICE_TYPE_PACKAGE, guestCount, null, packageSlotPrice, -1L);

    // then
    assertThat(totalPrice).isEqualTo(5000);
  }

  @Test
  @DisplayName("패키지 요금 계산, 기본 요금 5000, 추가 인원 5명, 한명 당 추가요금 3000이면 총 요금 20000원")
  void calculatePackageSlotPriceAdditionalFare() {

    // given
    int guestCount = 10;
    PackageSlotPrice packageSlotPrice = PackageSlotPrice.builder().price(5000).build();

    int standardCapacity = 5;
    ReservationPriceInfo reservationPriceInfo =
        ReservationPriceInfo.builder()
            .type(Constants.PRICE_TYPE_PACKAGE)
            .standardCapacity(standardCapacity)
            .perPersonAdditionalRate(3000)
            .isPerPersonRate(true)
            .build();

    when(reservationPriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            -1L, Constants.PRICE_TYPE_PACKAGE))
        .thenReturn(Optional.of(reservationPriceInfo));

    // when
    int totalPrice =
        reservationPriceCalculateService.calculateTotalPrice(
            Constants.PRICE_TYPE_PACKAGE, guestCount, null, packageSlotPrice, -1L);

    // then
    assertThat(totalPrice).isEqualTo(20000);
  }
}
