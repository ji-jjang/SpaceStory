package com.juny.spacestory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.juny.spacestory.domain.price.entity.BasePriceInformation;
import com.juny.spacestory.domain.price.entity.DayType;
import com.juny.spacestory.domain.price.entity.ExceptionPriceDetail;
import com.juny.spacestory.domain.price.entity.ExceptionPriceInformation;
import com.juny.spacestory.domain.price.entity.PackagePrice;
import com.juny.spacestory.domain.price.entity.PriceType;
import com.juny.spacestory.domain.price.entity.TimePrice;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    slotService.createTimeSlot(detailedSpaceId, creationMonth);

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
    slotService.createPackageSlot(detailedSpaceId, creationMonth);

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
}
