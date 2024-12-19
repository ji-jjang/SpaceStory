package com.juny.spacestory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.repository.ReservationRepository;
import com.juny.spacestory.domain.slot.dto.ResPrice;
import com.juny.spacestory.domain.slot.entity.BasePriceInformation;
import com.juny.spacestory.domain.slot.entity.DayType;
import com.juny.spacestory.domain.slot.entity.ExceptionPriceDetail;
import com.juny.spacestory.domain.slot.entity.ExceptionPriceInformation;
import com.juny.spacestory.domain.slot.entity.PackageDayPrice;
import com.juny.spacestory.domain.slot.entity.PackagePrice;
import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeDayPrice;
import com.juny.spacestory.domain.slot.entity.TimePrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.slot.repository.BasePriceInformationRepository;
import com.juny.spacestory.domain.slot.repository.ExceptionPriceInformationRepository;
import com.juny.spacestory.domain.slot.repository.PackageDayPriceRepository;
import com.juny.spacestory.domain.slot.repository.PackagePriceRepository;
import com.juny.spacestory.domain.slot.repository.PackageSlotPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeDayPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimePriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeSlotPriceRepository;
import com.juny.spacestory.domain.slot.service.SlotService;
import com.juny.spacestory.domain.space.entity.DetailedSpace;
import com.juny.spacestory.domain.space.entity.Space;
import com.juny.spacestory.domain.space.repository.SpaceRepository;
import com.juny.spacestory.global.constant.Constants;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SlotServiceTest {

  @Captor ArgumentCaptor<TimePrice> timePriceCaptor;
  @Captor ArgumentCaptor<PackagePrice> packagePriceCaptor;
  @Captor private ArgumentCaptor<List<Long>> updateIdsCaptor;
  @InjectMocks private SlotService slotService;
  @Mock private BasePriceInformationRepository basePriceInformationRepository;
  @Mock private ExceptionPriceInformationRepository exceptionPriceInformationRepository;
  @Mock private SpaceRepository spaceRepository;
  @Mock private TimePriceRepository timePriceRepository;
  @Mock private TimeDayPriceRepository timeDayPriceRepository;
  @Mock private TimeSlotPriceRepository timeSlotPriceRepository;
  @Mock private PackagePriceRepository packagePriceRepository;
  @Mock private PackageDayPriceRepository packageDayPriceRepository;
  @Mock private PackageSlotPriceRepository packageSlotPriceRepository;
  @Mock private ReservationRepository reservationRepository;
  @Mock private Clock clock;

  @Test
  @DisplayName("3개월 시간 슬롯을 생성한다. (기준일 24-12-15, 현재 달 포함 4개월)")
  public void createTimeSlotTest() {

    // given
    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    Long detailedSpaceId = -1L;
    int creationMonth = 3;
    LocalTime openingTime = LocalTime.of(8, 0);
    LocalTime closingTime = LocalTime.of(20, 0);
    List<YearMonth> targetYearMonths =
        List.of(
            YearMonth.of(2024, 12),
            YearMonth.of(2025, 1),
            YearMonth.of(2025, 2),
            YearMonth.of(2025, 3));
    Space space =
        Space.builder()
            .name("name1")
            .description("description1")
            .openingTime(openingTime)
            .closingTime(closingTime)
            .build();

    int price = 1000;
    int exceptionPrice = 500_000;
    List<BasePriceInformation> basePriceInformationList = new ArrayList<>();
    List<ExceptionPriceDetail> exceptionPriceDetails = new ArrayList<>();
    for (var dayType : DayType.values()) {

      LocalTime curTime = openingTime;
      while (curTime.isBefore(closingTime)) {

        BasePriceInformation basePriceInformation =
            BasePriceInformation.builder()
                .priceType(Constants.PRICE_TYPE_TIME)
                .dayType(dayType)
                .startTime(curTime)
                .price(price)
                .build();
        basePriceInformationList.add(basePriceInformation);

        ExceptionPriceDetail exceptionPriceDetail =
            ExceptionPriceDetail.builder()
                .priceType(Constants.PRICE_TYPE_TIME)
                .dayType(dayType)
                .startTime(curTime)
                .price(exceptionPrice)
                .build();
        exceptionPriceDetails.add(exceptionPriceDetail);

        curTime = curTime.plusMinutes(30);
        price += 1000;
        exceptionPrice += 10_000;
      }
    }

    List<ExceptionPriceInformation> exceptionPriceInformationList =
        List.of(
            ExceptionPriceInformation.builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 3))
                .exceptionPriceDetails(exceptionPriceDetails)
                .build());

    DetailedSpace.builder()
        .name("detailed name1")
        .size(30)
        .space(space)
        .basePriceInformation(basePriceInformationList)
        .exceptionPriceInformation(exceptionPriceInformationList)
        .build();

    when(spaceRepository.findByDetailedSpaceId(detailedSpaceId)).thenReturn(Optional.of(space));
    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME))
        .thenReturn(basePriceInformationList);
    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE))
        .thenReturn(Collections.emptyList());
    when(exceptionPriceInformationRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME))
        .thenReturn(exceptionPriceInformationList);
    when(timePriceRepository.existDetailedSpaceIdAndYearMonth(
            eq(detailedSpaceId), any(YearMonth.class)))
        .thenReturn(Boolean.TRUE);

    // when
    slotService.createSlots(detailedSpaceId, creationMonth);

    // then
    verify(timePriceRepository, Mockito.times(targetYearMonths.size()))
        .save(timePriceCaptor.capture());
    List<TimePrice> capturedTimePrices = timePriceCaptor.getAllValues();

    assertThat(capturedTimePrices.size()).isEqualTo(4);

    assertThat(capturedTimePrices.get(1).getYearAndMonth()).isEqualTo(YearMonth.of(2025, 1));

    assertThat(capturedTimePrices.get(0).getTimeDayPrices().size()).isEqualTo(17);

    assertThat(capturedTimePrices.get(1).getTimeDayPrices().size()).isEqualTo(31);

    assertThat(capturedTimePrices.get(0).getTimeDayPrices().getFirst().getTimeSlotPrices().size())
        .isEqualTo(24);

    assertThat(
            capturedTimePrices
                .get(1)
                .getTimeDayPrices()
                .getFirst()
                .getTimeSlotPrices()
                .getFirst()
                .getPrice())
        .isEqualTo(500_000);
    assertThat(
            capturedTimePrices
                .get(1)
                .getTimeDayPrices()
                .getFirst()
                .getTimeSlotPrices()
                .get(23)
                .getPrice())
        .isEqualTo(730_000);

    assertThat(
            capturedTimePrices
                .get(1)
                .getTimeDayPrices()
                .get(29)
                .getTimeSlotPrices()
                .getFirst()
                .getPrice())
        .isEqualTo(1_000);
    assertThat(
            capturedTimePrices
                .get(1)
                .getTimeDayPrices()
                .get(29)
                .getTimeSlotPrices()
                .get(23)
                .getPrice())
        .isEqualTo(24_000);
  }

  @Test
  @DisplayName("3개월 패키지 슬롯을 생성한다. (기준일 24-12-15, 현재 달 포함 4개월)")
  public void createPackagePricesTest() {

    // given
    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    Long detailedSpaceId = -1L;
    int creationMonth = 3;
    LocalTime openingTime = LocalTime.of(8, 0);
    LocalTime closingTime = LocalTime.of(20, 0);
    List<YearMonth> targetYearMonths =
        List.of(
            YearMonth.of(2024, 12),
            YearMonth.of(2025, 1),
            YearMonth.of(2025, 2),
            YearMonth.of(2025, 3));
    Space space =
        Space.builder()
            .name("name1")
            .description("description1")
            .openingTime(openingTime)
            .closingTime(closingTime)
            .build();

    int price = 1000;
    int exceptionPrice = 500_000;
    List<BasePriceInformation> basePriceInformationList = new ArrayList<>();
    List<ExceptionPriceDetail> exceptionPriceDetails = new ArrayList<>();

    List<LocalTime> startTimes = List.of(LocalTime.of(8, 0), LocalTime.of(23, 0));
    List<LocalTime> endTimes = List.of(LocalTime.of(20, 0), LocalTime.of(6, 0));

    for (var dayType : DayType.values()) {

      for (int i = 0; i < 2; ++i) {
        BasePriceInformation basePriceInformation =
            BasePriceInformation.builder()
                .priceType(Constants.PRICE_TYPE_PACKAGE)
                .dayType(dayType)
                .startTime(startTimes.get(i))
                .endTime(endTimes.get(i))
                .price(price)
                .build();
        basePriceInformationList.add(basePriceInformation);

        ExceptionPriceDetail exceptionPriceDetail =
            ExceptionPriceDetail.builder()
                .priceType(Constants.PRICE_TYPE_PACKAGE)
                .dayType(dayType)
                .startTime(startTimes.get(i))
                .endTime(endTimes.get(i))
                .price(exceptionPrice)
                .build();
        exceptionPriceDetails.add(exceptionPriceDetail);

        price += 1000;
        exceptionPrice += 10_000;
      }
    }

    List<ExceptionPriceInformation> exceptionPriceInformationList =
        List.of(
            ExceptionPriceInformation.builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 3))
                .exceptionPriceDetails(exceptionPriceDetails)
                .build());

    DetailedSpace.builder()
        .name("detailed name1")
        .size(30)
        .space(space)
        .basePriceInformation(basePriceInformationList)
        .exceptionPriceInformation(exceptionPriceInformationList)
        .build();

    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME))
        .thenReturn(Collections.emptyList());
    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE))
        .thenReturn(basePriceInformationList);
    when(exceptionPriceInformationRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE))
        .thenReturn(exceptionPriceInformationList);
    when(packagePriceRepository.existDetailedSpaceIdAndYearMonth(
            eq(detailedSpaceId), any(YearMonth.class)))
        .thenReturn(Boolean.TRUE);

    // when
    slotService.createSlots(detailedSpaceId, creationMonth);

    // then
    verify(packagePriceRepository, Mockito.times(targetYearMonths.size()))
        .save(packagePriceCaptor.capture());
    List<PackagePrice> capturedPackagePrices = packagePriceCaptor.getAllValues();

    assertThat(capturedPackagePrices.size()).isEqualTo(4);

    assertThat(capturedPackagePrices.get(1).getYearAndMonth()).isEqualTo(YearMonth.of(2025, 1));

    assertThat(capturedPackagePrices.get(0).getPackageDayPrices().size()).isEqualTo(17);

    assertThat(capturedPackagePrices.get(1).getPackageDayPrices().size()).isEqualTo(31);

    assertThat(
            capturedPackagePrices
                .get(0)
                .getPackageDayPrices()
                .getFirst()
                .getPackageSlotPrices()
                .size())
        .isEqualTo(2);

    assertThat(
            capturedPackagePrices
                .get(1)
                .getPackageDayPrices()
                .getFirst()
                .getPackageSlotPrices()
                .getFirst()
                .getPrice())
        .isEqualTo(500_000);

    assertThat(
            capturedPackagePrices
                .get(1)
                .getPackageDayPrices()
                .getFirst()
                .getPackageSlotPrices()
                .get(1)
                .getPrice())
        .isEqualTo(510_000);

    assertThat(
            capturedPackagePrices
                .get(1)
                .getPackageDayPrices()
                .get(29)
                .getPackageSlotPrices()
                .get(0)
                .getPrice())
        .isEqualTo(1_000);

    assertThat(
            capturedPackagePrices
                .get(1)
                .getPackageDayPrices()
                .get(29)
                .getPackageSlotPrices()
                .get(1)
                .getPrice())
        .isEqualTo(2_000);

    assertThat(
            capturedPackagePrices
                .get(1)
                .getPackageDayPrices()
                .getFirst()
                .getPackageSlotPrices()
                .get(1)
                .getStartTime())
        .isEqualTo(LocalDateTime.of(2025, 1, 1, 23, 0));

    assertThat(
            capturedPackagePrices
                .get(1)
                .getPackageDayPrices()
                .getFirst()
                .getPackageSlotPrices()
                .get(1)
                .getEndTime())
        .isEqualTo(LocalDateTime.of(2025, 1, 2, 6, 0));
  }

  @Test
  @DisplayName(
      "3개월 시간 슬롯을 조회한다. (기준일 24-12-15, 현재 달 포함 4개월), 12월은 12월 15일을 제외한 날은 모두 예약이 되었을 경우 가정")
  void getTimeSlots() {

    // given
    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    Long detailedSpaceId = -1L;
    final int month = 3;
    LocalTime openingTime = LocalTime.of(12, 0);
    LocalTime closingTime = LocalTime.of(15, 0);

    List<YearMonth> targetYearMonths =
        IntStream.rangeClosed(0, month).mapToObj(YearMonth.now(clock)::plusMonths).toList();

    List<TimePrice> timePrices = new ArrayList<>();
    for (var yearMonth : targetYearMonths) {
      List<TimeDayPrice> timeDayPrices = new ArrayList<>();
      for (int day = 1; day <= yearMonth.lengthOfMonth(); ++day) {
        LocalDate date = yearMonth.atDay(day);
        if (date.isBefore(LocalDate.now(clock))) {
          continue;
        }
        LocalTime curTime = openingTime;
        List<TimeSlotPrice> timeSlotPrices = new ArrayList<>();
        while (curTime.isBefore(closingTime)) {
          TimeSlotPrice timeSlotPrice;
          if (yearMonth.getMonth() == Month.DECEMBER && day == 15) {
            timeSlotPrice = new TimeSlotPrice(-1L, curTime, 1000, true);
          } else {
            timeSlotPrice = new TimeSlotPrice(-1L, curTime, 1000, false);
          }
          curTime = curTime.plusMinutes(30);
          timeSlotPrices.add(timeSlotPrice);
        }
        TimeDayPrice timeDayPrice = new TimeDayPrice(-1L, day, false, timeSlotPrices);
        timeDayPrices.add(timeDayPrice);
      }
      TimePrice timePrice = new TimePrice(-1L, yearMonth, timeDayPrices);
      timePrices.add(timePrice);
    }

    when(timePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
            detailedSpaceId, targetYearMonths))
        .thenReturn(timePrices);

    // when
    ResPrice slots = slotService.getSlots(detailedSpaceId, "time", 3);

    // then
    assertThat(slots.timePrices().size()).isEqualTo(4);

    assertThat(slots.timePrices().getFirst().dayTimePrices().size()).isEqualTo(17);

    assertThat(slots.timePrices().getFirst().dayTimePrices().getFirst().isAllReserved())
        .isEqualTo(true);

    assertThat(slots.timePrices().getFirst().dayTimePrices().get(1).isAllReserved())
        .isEqualTo(false);

    assertThat(slots.timePrices().getFirst().dayTimePrices().get(1).timeSlotPrices().size())
        .isEqualTo(6);
  }

  @Test
  @DisplayName(
      "3개월 패키지 슬롯을 조회한다. (기준일 24-12-15, 현재 달 포함 4개월), 12월은 12월 15일을 제외한 날은 모두 예약이 되었을 경우 가정")
  void getPackageSlots() {

    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    final int month = 3;
    List<LocalTime> startTimes = List.of(LocalTime.of(8, 0), LocalTime.of(23, 0));
    List<LocalTime> endTimes = List.of(LocalTime.of(20, 0), LocalTime.of(6, 0));

    List<YearMonth> targetYearMonths =
        IntStream.rangeClosed(0, month).mapToObj(YearMonth.now(clock)::plusMonths).toList();

    List<PackagePrice> packagePrices = new ArrayList<>();
    for (var yearMonth : targetYearMonths) {
      List<PackageDayPrice> packageDayPrices = new ArrayList<>();
      for (int day = 1; day <= yearMonth.lengthOfMonth(); ++day) {
        LocalDate date = yearMonth.atDay(day);
        if (date.isBefore(LocalDate.now(clock))) {
          continue;
        }
        List<PackageSlotPrice> packageSlotPrices = new ArrayList<>();

        for (int i = 0; i < startTimes.size(); ++i) {
          PackageSlotPrice packageSlotPrice;
          String name;
          int price;
          LocalDateTime startTime =
              LocalDateTime.of(
                  yearMonth.getYear(),
                  yearMonth.getMonth(),
                  day,
                  startTimes.get(i).getHour(),
                  startTimes.get(i).getMinute());
          LocalDateTime endTime =
              LocalDateTime.of(
                  yearMonth.getYear(),
                  yearMonth.getMonth(),
                  day,
                  endTimes.get(i).getHour(),
                  endTimes.get(i).getMinute());
          if (i == 0) {
            name = "아침부터 밤까지 풀타임 패키지";
            price = 10000;
          } else {
            name = "새벽시간 패키지";
            price = 5000;
            endTime = endTime.plusDays(1);
          }
          if (yearMonth.getMonth() == Month.DECEMBER && day == 15) {
            packageSlotPrice = new PackageSlotPrice(-1L, name, startTime, endTime, price, true);
          } else {
            packageSlotPrice = new PackageSlotPrice(-1L, name, startTime, endTime, price, false);
          }
          packageSlotPrices.add(packageSlotPrice);
        }
        PackageDayPrice packageDayPrice = new PackageDayPrice(-1L, day, packageSlotPrices);
        packageDayPrices.add(packageDayPrice);
      }
      PackagePrice packagePrice = new PackagePrice(-1L, yearMonth, packageDayPrices);
      packagePrices.add(packagePrice);
    }

    when(packagePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
            -1L, targetYearMonths))
        .thenReturn(packagePrices);

    // when
    ResPrice slots = slotService.getSlots(-1L, "package", month);

    // then
    assertThat(slots.packagePrices().size()).isEqualTo(4);

    assertThat(slots.packagePrices().getFirst().dayPackagePrices().size()).isEqualTo(17);

    assertThat(slots.packagePrices().getFirst().dayPackagePrices().getFirst().isAllReserved())
        .isEqualTo(true);

    assertThat(slots.packagePrices().getFirst().dayPackagePrices().get(1).isAllReserved())
        .isEqualTo(false);

    assertThat(
            slots.packagePrices().getFirst().dayPackagePrices().get(1).packageSlotPrices().size())
        .isEqualTo(2);
  }

  @Test
  @DisplayName(
      "시간 예약 정보 동기화해서 수정한다. 영업시간 8~20 12월 15일 8~12 패키지 예약과 15~16 시간제 예약이 되어있을 때, 10개의 예약된 슬롯 아이디를 찾는지 검증한다. (기준일 24-12-15)")
  public void updateTimeSlotWithSync() {

    // given
    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    Long detailedSpaceId = -1L;
    int month = 3;
    boolean isSync = true;

    List<YearMonth> targetYearMonths =
        IntStream.rangeClosed(0, month).mapToObj(YearMonth.now(clock)::plusMonths).toList();

    int price = 1000;
    int exceptionPrice = 500_000;
    List<BasePriceInformation> basePriceInformationList = new ArrayList<>();
    List<ExceptionPriceDetail> exceptionPriceDetails = new ArrayList<>();
    LocalTime openingTime = LocalTime.of(8, 0);
    LocalTime closingTime = LocalTime.of(20, 0);
    for (var dayType : DayType.values()) {

      LocalTime curTime = openingTime;
      while (curTime.isBefore(closingTime)) {

        BasePriceInformation basePriceInformation =
            BasePriceInformation.builder()
                .priceType(Constants.PRICE_TYPE_TIME)
                .dayType(dayType)
                .startTime(curTime)
                .price(price)
                .build();
        basePriceInformationList.add(basePriceInformation);

        ExceptionPriceDetail exceptionPriceDetail =
            ExceptionPriceDetail.builder()
                .priceType(Constants.PRICE_TYPE_TIME)
                .dayType(dayType)
                .startTime(curTime)
                .price(exceptionPrice)
                .build();
        exceptionPriceDetails.add(exceptionPriceDetail);

        curTime = curTime.plusMinutes(30);
        price += 1000;
        exceptionPrice += 10_000;
      }
    }

    Space space =
        Space.builder()
            .name("name1")
            .description("description1")
            .openingTime(openingTime)
            .closingTime(closingTime)
            .build();

    List<ExceptionPriceInformation> exceptionPriceInformationList =
        List.of(
            ExceptionPriceInformation.builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 3))
                .exceptionPriceDetails(exceptionPriceDetails)
                .build());

    LocalDateTime createdAt = LocalDateTime.of(2024, 12, 1, 17, 0);
    LocalDateTime startTime = LocalDateTime.of(2024, 12, 15, 8, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 12, 15, 12, 0);
    Reservation reservation1 =
        Reservation.builder()
            .startDateTime(startTime)
            .endDateTime(endTime)
            .guestCount(5)
            .totalPrice(5000)
            .createdAt(createdAt)
            .build();

    createdAt = LocalDateTime.of(2024, 12, 1, 17, 0);
    startTime = LocalDateTime.of(2024, 12, 15, 15, 0);
    endTime = LocalDateTime.of(2024, 12, 15, 16, 0);

    Reservation reservation2 =
        Reservation.builder()
            .startDateTime(startTime)
            .endDateTime(endTime)
            .guestCount(5)
            .totalPrice(5000)
            .createdAt(createdAt)
            .build();

    when(spaceRepository.findByDetailedSpaceId(detailedSpaceId))
        .thenReturn(Optional.ofNullable(space));

    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME))
        .thenReturn(basePriceInformationList);

    when(exceptionPriceInformationRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME))
        .thenReturn(exceptionPriceInformationList);

    when(timePriceRepository.existDetailedSpaceIdAndYearMonth(
            eq(detailedSpaceId), any(YearMonth.class)))
        .thenReturn(Boolean.TRUE);

    when(reservationRepository.findAllDetailedSpaceIdAndYearMonths(
            detailedSpaceId, targetYearMonths))
        .thenReturn(List.of(reservation1, reservation2));

    // when
    slotService.updateSlots(detailedSpaceId, month, isSync);
    verify(timeSlotPriceRepository).updateIsReservedByIds(updateIdsCaptor.capture());
    List<Long> capturedUpdateIds = updateIdsCaptor.getValue();

    // then
    assertThat(capturedUpdateIds.size()).isEqualTo(10);
  }

  @Test
  @DisplayName(
      "패키지 예약 정보 동기화해서 수정한다. 영업 시간 8~20 변경하고 12월 15일 8~12 패키지 예약이 되어있을 때, 2개의 예약된 슬롯 아이디를 찾는지 검증한다. (기준일 24-12-15)")
  public void updatePackageSlotWithSync() {

    // given
    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
        .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    Long detailedSpaceId = -1L;
    int month = 3;
    boolean isSync = true;

    List<YearMonth> targetYearMonths =
        IntStream.rangeClosed(0, month).mapToObj(YearMonth.now(clock)::plusMonths).toList();

    int price = 1000;
    int exceptionPrice = 500_000;
    List<BasePriceInformation> basePriceInformationList = new ArrayList<>();
    List<ExceptionPriceDetail> exceptionPriceDetails = new ArrayList<>();
    LocalTime openingTime = LocalTime.of(8, 0);
    LocalTime closingTime = LocalTime.of(20, 0);
    for (var dayType : DayType.values()) {

      BasePriceInformation basePriceInformation =
          BasePriceInformation.builder()
              .priceType(Constants.PRICE_TYPE_PACKAGE)
              .name("오전 패키지 입니다!!")
              .dayType(dayType)
              .startTime(LocalTime.of(8, 0))
              .endTime(LocalTime.of(12, 0))
              .price(price)
              .build();
      basePriceInformationList.add(basePriceInformation);

      ExceptionPriceDetail exceptionPriceDetail =
          ExceptionPriceDetail.builder()
              .priceType(Constants.PRICE_TYPE_PACKAGE)
              .dayType(dayType)
              .startTime(LocalTime.of(17, 0))
              .endTime(LocalTime.of(19, 0))
              .price(exceptionPrice)
              .build();
      exceptionPriceDetails.add(exceptionPriceDetail);

      price += 1000;
      exceptionPrice += 10_000;
    }

    Space space =
        Space.builder()
            .name("name1")
            .description("description1")
            .openingTime(openingTime)
            .closingTime(closingTime)
            .build();

    List<ExceptionPriceInformation> exceptionPriceInformationList =
        List.of(
            ExceptionPriceInformation.builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 3))
                .exceptionPriceDetails(exceptionPriceDetails)
                .build());

    LocalDateTime createdAt = LocalDateTime.of(2024, 12, 1, 17, 0);
    LocalDateTime startTime = LocalDateTime.of(2024, 12, 15, 8, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 12, 15, 12, 0);
    Reservation reservation =
        Reservation.builder()
            .startDateTime(startTime)
            .endDateTime(endTime)
            .guestCount(5)
            .totalPrice(5000)
            .createdAt(createdAt)
            .build();

    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME))
        .thenReturn(Collections.emptyList());

    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE))
        .thenReturn(basePriceInformationList);

    when(reservationRepository.findAllDetailedSpaceIdAndYearMonths(
            detailedSpaceId, targetYearMonths))
        .thenReturn(List.of(reservation));

    when(exceptionPriceInformationRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE))
        .thenReturn(exceptionPriceInformationList);

    when(packagePriceRepository.existDetailedSpaceIdAndYearMonth(
            eq(detailedSpaceId), any(YearMonth.class)))
        .thenReturn(Boolean.TRUE);

    // when
    slotService.updateSlots(detailedSpaceId, month, isSync);
    verify(packageSlotPriceRepository).updateIsReservedByIds(updateIdsCaptor.capture());
    List<Long> capturedUpdateIds = updateIdsCaptor.getValue();

    // then
    assertThat(capturedUpdateIds.size()).isEqualTo(1);
  }
}
