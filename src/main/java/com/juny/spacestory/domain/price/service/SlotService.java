package com.juny.spacestory.domain.price.service;

import com.juny.spacestory.domain.price.dto.ReqCreateSlot;
import com.juny.spacestory.domain.price.entity.DayPackagePrice;
import com.juny.spacestory.domain.price.entity.DayTimePrice;
import com.juny.spacestory.domain.price.entity.DayType;
import com.juny.spacestory.domain.price.entity.ExceptionPriceInformation;
import com.juny.spacestory.domain.price.entity.PackagePrice;
import com.juny.spacestory.domain.price.entity.PackageSlotPrice;
import com.juny.spacestory.domain.price.entity.BasePriceInformation;
import com.juny.spacestory.domain.price.entity.PriceInfo;
import com.juny.spacestory.domain.price.entity.PriceType;
import com.juny.spacestory.domain.price.entity.TimePrice;
import com.juny.spacestory.domain.price.entity.TimeSlotPrice;
import com.juny.spacestory.domain.price.repository.DayPackageRepository;
import com.juny.spacestory.domain.price.repository.DayTimeRepository;
import com.juny.spacestory.domain.price.repository.ExceptionPriceInformationRepository;
import com.juny.spacestory.domain.price.repository.PackagePriceRepository;
import com.juny.spacestory.domain.price.repository.PackageSlotRepository;
import com.juny.spacestory.domain.price.repository.BasePriceInformationRepository;
import com.juny.spacestory.domain.price.repository.TimePriceRepository;
import com.juny.spacestory.domain.price.repository.TimeSlotRepository;
import com.juny.spacestory.domain.space.entity.Space;
import com.juny.spacestory.domain.space.repository.SpaceRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SlotService {

  private final BasePriceInformationRepository basePriceInfoRepository;

  private final ExceptionPriceInformationRepository exceptionPriceInfoRepository;

  private final SpaceRepository spaceRepository;

  private final TimePriceRepository timePriceRepository;

  private final DayTimeRepository dayTimeRepository;

  private final TimeSlotRepository timeSlotRepository;

  private final PackagePriceRepository packagePriceRepository;

  private final DayPackageRepository dayPackageRepository;

  private final PackageSlotRepository packageSlotRepository;

  private final Clock clock;

  @Transactional
  public void createSlots(Long detailedSpaceId, int creationMonth, ReqCreateSlot req) {

    if (req.createTimeSlot()) {
      createTimeSlot(detailedSpaceId, creationMonth);
    }

    if (req.createPackageSlot()) {
      createPackageSlot(detailedSpaceId, creationMonth);
    }
  }

  public void createTimeSlot(Long detailedSpaceId, int creationMonth) {

    Space space = spaceRepository.findByDetailedSpaceId(detailedSpaceId).orElseThrow(
      () -> new RuntimeException("유효한 상세 공간 ID가 아닙니다."));

    List<BasePriceInformation> basePriceInformationList = basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.TIME.getNum());
    if (basePriceInformationList == null || basePriceInformationList.isEmpty()) {
      throw new RuntimeException("시간제 가격 정보가 없습니다.");
    }

    List<ExceptionPriceInformation> exceptionPriceInfoList = exceptionPriceInfoRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.TIME.getNum());
    List<YearMonth> targetYearMonths = IntStream.rangeClosed(0, creationMonth)
      .mapToObj(YearMonth.now(clock)::plusMonths)
      .toList();
    LocalTime openingTime = space.getOpeningTime();
    LocalTime closingTime = space.getClosingTime();
    for (var yearMonth : targetYearMonths) {

      if (!timePriceRepository.existDetailedSpaceIdAndYearMonth(detailedSpaceId, yearMonth)) {
        continue;
      }

      List<ExceptionPriceInformation> targetExceptionPriceInfoList = exceptionPriceInfoList.stream()
        .filter(info -> YearMonth.from(info.getStartDate()).equals(yearMonth)).toList();

      List<DayTimePrice> dayTimePrices = createDayTimePrices(yearMonth, openingTime, closingTime,
        basePriceInformationList, targetExceptionPriceInfoList);

      TimePrice price = new TimePrice(null, yearMonth, dayTimePrices);
      timePriceRepository.save(price);
    }
  }

  public void createPackageSlot(Long detailedSpaceId, int creationMonth) {

    List<BasePriceInformation> priceInfoList = basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.PACKAGE.getNum());

    if (priceInfoList == null || priceInfoList.isEmpty()) {
      throw new RuntimeException("패키지 가격 정보가 없습니다.");
    }

    List<BasePriceInformation> packagePriceInfoList = priceInfoList.stream()
      .filter(info -> info.getPriceType() == PriceType.PACKAGE.getNum())
      .toList();

    List<ExceptionPriceInformation> exceptionPriceInfoList = exceptionPriceInfoRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.PACKAGE.getNum());
    List<YearMonth> targetYearMonths = IntStream.rangeClosed(0, creationMonth)
      .mapToObj(YearMonth.now(clock)::plusMonths)
      .toList();
    for (var yearMonth : targetYearMonths) {

      if (!packagePriceRepository.existDetailedSpaceIdAndYearMonth(detailedSpaceId, yearMonth)) {
        continue;
      }

      List<ExceptionPriceInformation> targetExceptionPriceInfoList = exceptionPriceInfoList.stream()
        .filter(info -> YearMonth.from(info.getStartDate()).equals(yearMonth)).toList();

      List<DayPackagePrice> dayPackagePrices = createDayPackagePrices(yearMonth,
        packagePriceInfoList, targetExceptionPriceInfoList);

      PackagePrice price = new PackagePrice(null, yearMonth, dayPackagePrices);
      packagePriceRepository.save(price);
    }
  }

  private List<DayTimePrice> createDayTimePrices(YearMonth yearMonth, LocalTime openingTime,
    LocalTime closingTime, List<BasePriceInformation> basePriceInfoList,
    List<ExceptionPriceInformation> exceptionPriceInfoList) {

    List<DayTimePrice> dayTimePrices = new ArrayList<>();
    for (var exceptionPriceInfo : exceptionPriceInfoList) {

      LocalDate curDay = exceptionPriceInfo.getStartDate();
      LocalDate endDay = exceptionPriceInfo.getEndDate();
      while (!curDay.isAfter(endDay)) {

        createDayTimePrice(openingTime, closingTime, exceptionPriceInfo.getExceptionPriceDetails(),
          curDay,
          dayTimePrices);
        curDay = curDay.plusDays(1);
      }
    }

    List<LocalDate> basePriceDate = getBasePriceDate(yearMonth, exceptionPriceInfoList);
    for (var date : basePriceDate) {

      createDayTimePrice(openingTime, closingTime, basePriceInfoList, date, dayTimePrices);
    }

    return dayTimePrices;
  }


  private List<LocalDate> getBasePriceDate(YearMonth yearMonth,
    List<ExceptionPriceInformation> exceptionPriceInfoList) {

    return IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
      .filter(day -> {
        LocalDate date = yearMonth.atDay(day);
        return !date.isBefore(LocalDate.now(clock)) &&
          exceptionPriceInfoList.stream()
            .noneMatch(info -> isDateInRange(day, info.getStartDate(), info.getEndDate()));
      })
      .boxed()
      .sorted()
      .map(yearMonth::atDay)
      .toList();
  }

  private boolean isDateInRange(int day, LocalDate startDate, LocalDate endDate) {
    return day >= startDate.getDayOfMonth() && day <= endDate.getDayOfMonth();
  }

  private void createDayTimePrice(LocalTime openingTime, LocalTime closingTime,
    List<? extends PriceInfo> priceInfoList, LocalDate curDay,
    List<DayTimePrice> dayTimePrices) {

    List<TimeSlotPrice> timeSlotPrices = createTimeSlotPrices(DayType.fromLocalDate(curDay),
      openingTime,
      closingTime, priceInfoList);

    DayTimePrice dayTimePrice = new DayTimePrice(null, curDay.getDayOfMonth(), false,
      timeSlotPrices);
    dayTimeRepository.save(dayTimePrice);
    dayTimePrices.add(dayTimePrice);
  }

  private List<TimeSlotPrice> createTimeSlotPrices(DayType curDayType,
    LocalTime openingTime, LocalTime closingTime,
    List<? extends PriceInfo> basePriceInformationList) {

    List<TimeSlotPrice> timeSlotPrices = new ArrayList<>();
    LocalTime curTime = openingTime;

    while (curTime.isBefore(closingTime)) {

      LocalTime finalCurTime = curTime;

      PriceInfo curTimeBasePriceInformation = basePriceInformationList.stream()
        .filter(info -> info.getDayType() == curDayType.getNum() && info.getStartTime()
          .equals(finalCurTime))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("해당 시간에 해당하는 시간제 가격 정보가 없습니다."));

      TimeSlotPrice timeSlotPrice = new TimeSlotPrice(null, curTime,
        curTimeBasePriceInformation.getPrice(), false,
        null);
      timeSlotRepository.save(timeSlotPrice);

      timeSlotPrices.add(timeSlotPrice);
      curTime = curTime.plusMinutes(30);
    }

    return timeSlotPrices;
  }

  private List<DayPackagePrice> createDayPackagePrices(YearMonth yearMonth,
    List<BasePriceInformation> packagePriceInfoList,
    List<ExceptionPriceInformation> exceptionPriceInfoList) {

    List<DayPackagePrice> dayPackagePrices = new ArrayList<>();
    for (var exceptionPriceInfo : exceptionPriceInfoList) {

      LocalDate curDay = exceptionPriceInfo.getStartDate();
      LocalDate endDay = exceptionPriceInfo.getEndDate();
      while (!curDay.isAfter(endDay)) {

        createDayPackagePrice(exceptionPriceInfo.getExceptionPriceDetails(), curDay,
          dayPackagePrices);
        curDay = curDay.plusDays(1);
      }
    }

    List<LocalDate> basePriceDate = getBasePriceDate(yearMonth, exceptionPriceInfoList);
    for (var date : basePriceDate) {

      createDayPackagePrice(packagePriceInfoList, date, dayPackagePrices);
    }

    return dayPackagePrices;
  }

  private void createDayPackagePrice(List<? extends PriceInfo> exceptionPriceDetails,
    LocalDate curDay,
    List<DayPackagePrice> dayPackagePrices) {

    List<PackageSlotPrice> packageSlotPrices = createPackageSlotPrices(
      exceptionPriceDetails, curDay);

    DayPackagePrice dayPackagePrice = new DayPackagePrice(null, curDay.getDayOfMonth(), false,
      packageSlotPrices);
    dayPackageRepository.save(dayPackagePrice);
    dayPackagePrices.add(dayPackagePrice);
  }

  private List<PackageSlotPrice> createPackageSlotPrices(
    List<? extends PriceInfo> packagePriceInfoList,
    LocalDate curDay) {

    List<PackageSlotPrice> dayPackagePrices = new ArrayList<>();
    LocalDate endDay = curDay;

    List<? extends PriceInfo> list = packagePriceInfoList.stream().filter(
      info -> info.getDayType().equals(DayType.fromLocalDate(curDay).getNum())).toList();

    for (var priceInfo : list) {
      if (!priceInfo.getStartTime().isBefore(priceInfo.getEndTime())) {
        endDay = curDay.plusDays(1);
      }
      PackageSlotPrice packageSlotPrice = new PackageSlotPrice(null, priceInfo.getName(),
        LocalDateTime.of(curDay, priceInfo.getStartTime()),
        LocalDateTime.of(endDay, priceInfo.getEndTime()), priceInfo.getPrice(), false, null);
      packageSlotRepository.save(packageSlotPrice);
      dayPackagePrices.add(packageSlotPrice);
    }
    return dayPackagePrices;
  }
}
