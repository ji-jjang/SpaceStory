package com.juny.spacestory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.juny.spacestory.domain.price.dto.ReqCreateSlot;
import com.juny.spacestory.domain.price.dto.ResPrice;
import com.juny.spacestory.domain.price.entity.BasePriceInformation;
import com.juny.spacestory.domain.price.entity.DayPackagePrice;
import com.juny.spacestory.domain.price.entity.DayTimePrice;
import com.juny.spacestory.domain.price.entity.DayType;
import com.juny.spacestory.domain.price.entity.ExceptionPriceDetail;
import com.juny.spacestory.domain.price.entity.ExceptionPriceInformation;
import com.juny.spacestory.domain.price.entity.PackagePrice;
import com.juny.spacestory.domain.price.entity.PackageSlotPrice;
import com.juny.spacestory.domain.price.entity.PriceType;
import com.juny.spacestory.domain.price.entity.TimePrice;
import com.juny.spacestory.domain.price.entity.TimeSlotPrice;
import com.juny.spacestory.domain.price.repository.BasePriceInformationRepository;
import com.juny.spacestory.domain.price.repository.DayPackageRepository;
import com.juny.spacestory.domain.price.repository.DayTimeRepository;
import com.juny.spacestory.domain.price.repository.ExceptionPriceInformationRepository;
import com.juny.spacestory.domain.price.repository.PackagePriceRepository;
import com.juny.spacestory.domain.price.repository.PackageSlotRepository;
import com.juny.spacestory.domain.price.repository.TimePriceRepository;
import com.juny.spacestory.domain.price.repository.TimeSlotRepository;
import com.juny.spacestory.domain.price.service.SlotService;
import com.juny.spacestory.domain.space.entity.DetailedSpace;
import com.juny.spacestory.domain.space.entity.Space;
import com.juny.spacestory.domain.space.repository.SpaceRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
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

  @Captor
  ArgumentCaptor<TimePrice> timePriceCaptor;
  @Captor
  ArgumentCaptor<PackagePrice> packagePriceCaptor;
  @InjectMocks
  private SlotService slotService;
  @Mock
  private BasePriceInformationRepository basePriceInformationRepository;
  @Mock
  private ExceptionPriceInformationRepository exceptionPriceInformationRepository;
  @Mock
  private SpaceRepository spaceRepository;
  @Mock
  private TimePriceRepository timePriceRepository;
  @Mock
  private DayTimeRepository dayTimeRepository;
  @Mock
  private TimeSlotRepository timeSlotRepository;
  @Mock
  private PackagePriceRepository packagePriceRepository;
  @Mock
  private DayPackageRepository dayPackageRepository;
  @Mock
  private PackageSlotRepository packageSlotRepository;
  @Mock
  private Clock clock;

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
    List<YearMonth> targetYearMonths = List.of(YearMonth.of(2024, 12), YearMonth.of(2025, 1),
      YearMonth.of(2025, 2), YearMonth.of(2025, 3));
    Space space = Space.builder()
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
      while (!curTime.isAfter(closingTime)) {

        BasePriceInformation basePriceInformation = BasePriceInformation.builder()
          .priceType(PriceType.TIME)
          .dayType(dayType)
          .startTime(curTime)
          .price(price)
          .build();
        basePriceInformationList.add(basePriceInformation);

        ExceptionPriceDetail exceptionPriceDetail = ExceptionPriceDetail.builder()
          .priceType(PriceType.TIME)
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

    List<ExceptionPriceInformation> exceptionPriceInformationList = List.of(
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
    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(detailedSpaceId,
      PriceType.TIME.getNum())).thenReturn(basePriceInformationList);
    when(exceptionPriceInformationRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.TIME.getNum())).thenReturn(exceptionPriceInformationList);
    when(timePriceRepository.existDetailedSpaceIdAndYearMonth(eq(detailedSpaceId),
      any(YearMonth.class))).thenReturn(
      Boolean.TRUE);

    // when
    slotService.createSlots(detailedSpaceId, creationMonth, new ReqCreateSlot(true, false));

    // then
    Mockito.verify(timePriceRepository, Mockito.times(targetYearMonths.size()))
      .save(timePriceCaptor.capture());
    List<TimePrice> capturedTimePrices = timePriceCaptor.getAllValues();

    assertThat(capturedTimePrices.size()).isEqualTo(4);

    assertThat(capturedTimePrices.get(1).getYearAndMonth()).isEqualTo(YearMonth.of(2025, 1));

    assertThat(capturedTimePrices.get(0).getDayTimePrices().size()).isEqualTo(17);

    assertThat(capturedTimePrices.get(1).getDayTimePrices().size()).isEqualTo(31);

    assertThat(
      capturedTimePrices.get(0).getDayTimePrices().getFirst().getTimeSlotPrices().size()).isEqualTo(
      24);

    assertThat(
      capturedTimePrices.get(1).getDayTimePrices().getFirst().getTimeSlotPrices().getFirst()
        .getPrice()).isEqualTo(
      500_000);
    assertThat(
      capturedTimePrices.get(1).getDayTimePrices().getFirst().getTimeSlotPrices().get(23)
        .getPrice()).isEqualTo(
      730_000);

    assertThat(
      capturedTimePrices.get(1).getDayTimePrices().get(29).getTimeSlotPrices().getFirst()
        .getPrice()).isEqualTo(
      1_000);
    assertThat(
      capturedTimePrices.get(1).getDayTimePrices().get(29).getTimeSlotPrices().get(23)
        .getPrice()).isEqualTo(
      24_000);
  }

  @Test
  @DisplayName("3개월 패키지 슬롯을 생성한다. (기준일 24-12-15, 현재 달 포함 4개월)")
  public void createPackageSlotTest() {

    // given
    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
      .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    Long detailedSpaceId = -1L;
    int creationMonth = 3;
    LocalTime openingTime = LocalTime.of(8, 0);
    LocalTime closingTime = LocalTime.of(20, 0);
    List<YearMonth> targetYearMonths = List.of(YearMonth.of(2024, 12), YearMonth.of(2025, 1),
      YearMonth.of(2025, 2), YearMonth.of(2025, 3));
    Space space = Space.builder()
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
        BasePriceInformation basePriceInformation = BasePriceInformation.builder()
          .priceType(PriceType.PACKAGE)
          .dayType(dayType)
          .startTime(startTimes.get(i))
          .endTime(endTimes.get(i))
          .price(price)
          .build();
        basePriceInformationList.add(basePriceInformation);

        ExceptionPriceDetail exceptionPriceDetail = ExceptionPriceDetail.builder()
          .priceType(PriceType.PACKAGE)
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

    List<ExceptionPriceInformation> exceptionPriceInformationList = List.of(
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

    when(basePriceInformationRepository.findByDetailedSpaceIdAndPriceType(detailedSpaceId,
      PriceType.PACKAGE.getNum())).thenReturn(basePriceInformationList);
    when(exceptionPriceInformationRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.PACKAGE.getNum())).thenReturn(exceptionPriceInformationList);
    when(packagePriceRepository.existDetailedSpaceIdAndYearMonth(eq(detailedSpaceId),
      any(YearMonth.class))).thenReturn(Boolean.TRUE);

    // when
    slotService.createSlots(detailedSpaceId, creationMonth, new ReqCreateSlot(false, true));

    // then
    Mockito.verify(packagePriceRepository, Mockito.times(targetYearMonths.size()))
      .save(packagePriceCaptor.capture());
    List<PackagePrice> capturedPackagePrices = packagePriceCaptor.getAllValues();

    assertThat(capturedPackagePrices.size()).isEqualTo(4);

    assertThat(capturedPackagePrices.get(1).getYearAndMonth()).isEqualTo(YearMonth.of(2025, 1));

    assertThat(capturedPackagePrices.get(0).getDayPackagePrices().size()).isEqualTo(17);

    assertThat(capturedPackagePrices.get(1).getDayPackagePrices().size()).isEqualTo(31);

    assertThat(capturedPackagePrices.get(0).getDayPackagePrices().getFirst().getPackageSlotPrices()
      .size()).isEqualTo(2);

    assertThat(
      capturedPackagePrices.get(1).getDayPackagePrices().getFirst().getPackageSlotPrices()
        .getFirst()
        .getPrice()).isEqualTo(500_000);

    assertThat(
      capturedPackagePrices.get(1).getDayPackagePrices().getFirst().getPackageSlotPrices().get(1)
        .getPrice()).isEqualTo(510_000);

    assertThat(
      capturedPackagePrices.get(1).getDayPackagePrices().get(29).getPackageSlotPrices().get(0)
        .getPrice()).isEqualTo(1_000);

    assertThat(
      capturedPackagePrices.get(1).getDayPackagePrices().get(29).getPackageSlotPrices().get(1)
        .getPrice()).isEqualTo(2_000);

    assertThat(
      capturedPackagePrices.get(1).getDayPackagePrices().getFirst().getPackageSlotPrices().get(1)
        .getStartTime()).isEqualTo(
      LocalDateTime.of(2025, 1, 1, 23, 0));

    assertThat(
      capturedPackagePrices.get(1).getDayPackagePrices().getFirst().getPackageSlotPrices().get(1)
        .getEndTime()).isEqualTo(
      LocalDateTime.of(2025, 1, 2, 6, 0));
  }

  @Test
  @DisplayName("3개월 시간 슬롯을 조회한다. (기준일 24-12-15, 현재 달 포함 4개월), 12월은 12월 15일을 제외한 날은 모두 예약이 되었을 경우 가정")
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

    List<YearMonth> targetYearMonths = IntStream.rangeClosed(0, month)
      .mapToObj(YearMonth.now(clock)::plusMonths)
      .toList();

    List<TimePrice> timePrices = new ArrayList<>();
    for (var yearMonth : targetYearMonths) {
      List<DayTimePrice> dayTimePrices = new ArrayList<>();
      for (int day = 1; day <= yearMonth.lengthOfMonth(); ++day) {
        LocalDate date = yearMonth.atDay(day);
        if (date.isBefore(LocalDate.now(clock))) {
          continue;
        }
        LocalTime curTime = openingTime;
        List<TimeSlotPrice> timeSlotPrices = new ArrayList<>();
        while (!curTime.isAfter(closingTime)) {
          TimeSlotPrice timeSlotPrice;
          if (yearMonth.getMonth() == Month.DECEMBER && day == 15) {
            timeSlotPrice = new TimeSlotPrice(-1L, curTime, 1000, true, null);
          } else {
            timeSlotPrice = new TimeSlotPrice(-1L, curTime, 1000, false, null);
          }
          curTime = curTime.plusMinutes(30);
          timeSlotPrices.add(timeSlotPrice);
        }
        DayTimePrice dayTimePrice = new DayTimePrice(-1L, day, false, timeSlotPrices);
        dayTimePrices.add(dayTimePrice);
      }
      TimePrice timePrice = new TimePrice(-1L, yearMonth, dayTimePrices);
      timePrices.add(timePrice);
    }

    when(timePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(detailedSpaceId,
      targetYearMonths)).thenReturn(timePrices);

    // when
    ResPrice slots = slotService.getSlots(detailedSpaceId, "time", 3);

    // then
    assertThat(slots.timePrices().size()).isEqualTo(4);

    assertThat(slots.timePrices().getFirst().dayTimePrices().size()).isEqualTo(17);

    assertThat(slots.timePrices().getFirst().dayTimePrices().getFirst().isAllReserved()).isEqualTo(
      true);

    assertThat(slots.timePrices().getFirst().dayTimePrices().get(1).isAllReserved()).isEqualTo(
      false);

    assertThat(
      slots.timePrices().getFirst().dayTimePrices().get(1).timeSlotPrices().size()).isEqualTo(7);
  }

  @Test
  @DisplayName("3개월 패키지 슬롯을 조회한다. (기준일 24-12-15, 현재 달 포함 4개월), 12월은 12월 15일을 제외한 날은 모두 예약이 되었을 경우 가정")
  void getPackageSlots() {

    LocalDate fixedDate = LocalDate.of(2024, 12, 15);
    Mockito.when(clock.instant())
      .thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    final int month = 3;
    List<LocalTime> startTimes = List.of(LocalTime.of(8, 0), LocalTime.of(23, 0));
    List<LocalTime> endTimes = List.of(LocalTime.of(20, 0), LocalTime.of(6, 0));

    List<YearMonth> targetYearMonths = IntStream.rangeClosed(0, month)
      .mapToObj(YearMonth.now(clock)::plusMonths)
      .toList();

    List<PackagePrice> packagePrices = new ArrayList<>();
    for (var yearMonth : targetYearMonths) {
      List<DayPackagePrice> dayPackagePrices = new ArrayList<>();
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
          LocalDateTime startTime = LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), day,
            startTimes.get(i).getHour(), startTimes.get(i).getMinute());
          LocalDateTime endTime = LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), day,
            endTimes.get(i).getHour(), endTimes.get(i).getMinute());
          if (i == 0) {
            name = "아침부터 밤까지 풀타임 패키지";
            price = 10000;
          } else {
            name = "새벽시간 패키지";
            price = 5000;
            endTime = endTime.plusDays(1);
          }
          if (yearMonth.getMonth() == Month.DECEMBER && day == 15) {
            packageSlotPrice = new PackageSlotPrice(-1L, name, startTime, endTime,
              price, true, null);
          } else {
            packageSlotPrice = new PackageSlotPrice(-1L, name, startTime, endTime,
              price, false, null);
          }
          packageSlotPrices.add(packageSlotPrice);
        }
        DayPackagePrice dayPackagePrice = new DayPackagePrice(-1L, day, packageSlotPrices);
        dayPackagePrices.add(dayPackagePrice);
      }
      PackagePrice packagePrice = new PackagePrice(-1L, yearMonth, dayPackagePrices);
      packagePrices.add(packagePrice);
    }

    when(packagePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
      -1L, targetYearMonths)).thenReturn(packagePrices);

    // when
    ResPrice slots = slotService.getSlots(-1L, "package", month);

    // then
    assertThat(slots.packagePrices().size()).isEqualTo(4);

    assertThat(slots.packagePrices().getFirst().dayPackagePrices().size()).isEqualTo(17);

    assertThat(
      slots.packagePrices().getFirst().dayPackagePrices().getFirst().isAllReserved()).isEqualTo(
      true);

    assertThat(
      slots.packagePrices().getFirst().dayPackagePrices().get(1).isAllReserved()).isEqualTo(
      false);

    assertThat(
      slots.packagePrices().getFirst().dayPackagePrices().get(1).packageSlotPrices()
        .size()).isEqualTo(2);
  }
}
