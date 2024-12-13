package com.juny.spacestory.domain.price.service;

import com.juny.spacestory.domain.price.dto.ResPackagePrice;
import com.juny.spacestory.domain.price.dto.ResPrice;
import com.juny.spacestory.domain.price.dto.ResTimePrice;
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
import com.juny.spacestory.domain.price.mapper.SlotMapper;
import com.juny.spacestory.domain.price.repository.DayPackageRepository;
import com.juny.spacestory.domain.price.repository.DayTimeRepository;
import com.juny.spacestory.domain.price.repository.ExceptionPriceInformationRepository;
import com.juny.spacestory.domain.price.repository.PackagePriceRepository;
import com.juny.spacestory.domain.price.repository.PackageSlotRepository;
import com.juny.spacestory.domain.price.repository.BasePriceInformationRepository;
import com.juny.spacestory.domain.price.repository.TimePriceRepository;
import com.juny.spacestory.domain.price.repository.TimeSlotRepository;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.repository.ReservationRepository;
import com.juny.spacestory.domain.space.entity.Space;
import com.juny.spacestory.domain.space.repository.SpaceRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
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

  private final ReservationRepository reservationRepository;

  private final Clock clock;

  private static List<Reservation> getFilteredReservations(List<Reservation> reservations, int year,
    int month, int day) {

    return reservations.stream()
      .filter(reservation -> {
        LocalDate reservationDate = reservation.getStartTime().toLocalDate();
        return reservationDate.getYear() == year &&
          reservationDate.getMonthValue() == month &&
          reservationDate.getDayOfMonth() == day;
      })
      .toList();
  }

  /**
   * <h1> 시간제, 패키지 슬롯 생성 </h1>
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month           생성할 개월 수 (기본 3개월)
   * @return ResPrice
   */
  @Transactional
  public ResPrice createSlots(Long detailedSpaceId, int month) {

    List<ResTimePrice> resTimePrices = null;
    List<ResPackagePrice> resPackagePrices = null;

    List<BasePriceInformation> timePriceInfoList = basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.TIME.getNum());

    if (!timePriceInfoList.isEmpty()) {
      resTimePrices = createTimePrices(detailedSpaceId, month);
    }

    List<BasePriceInformation> packagePriceInfoList = basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.PACKAGE.getNum());

    if (!packagePriceInfoList.isEmpty()) {
      resPackagePrices = createPackagePrices(detailedSpaceId, month);
    }

    return new ResPrice(resTimePrices, resPackagePrices);
  }

  /**
   * <h1> 시간제 슬롯 가격 생성 </h1>
   *
   * <br>
   * - 다른 메서드에서 재사용하고, Transaction 보장하기 위해 public 메서드 사용
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month           생성할 개월 수
   * @return ResTimePrice
   */
  public List<ResTimePrice> createTimePrices(Long detailedSpaceId, int month) {

    Space space = spaceRepository.findByDetailedSpaceId(detailedSpaceId).orElseThrow(
      () -> new RuntimeException("유효한 상세 공간 ID가 아닙니다."));

    List<BasePriceInformation> basePriceInformationList = basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.TIME.getNum());
    if (basePriceInformationList == null || basePriceInformationList.isEmpty()) {
      throw new RuntimeException("시간제 가격 정보가 없습니다.");
    }

    List<ExceptionPriceInformation> exceptionPriceInfoList = exceptionPriceInfoRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.TIME.getNum());
    List<YearMonth> targetYearMonths = getYearMonths(month);
    LocalTime openingTime = space.getOpeningTime();
    LocalTime closingTime = space.getClosingTime();
    List<TimePrice> timePrices = new ArrayList<>();
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
      timePrices.add(price);
    }

    return SlotMapper.toResTimePrice(timePrices);
  }

  /**
   * <h1> 패키지 슬롯 가격 생성 </h1>
   *
   * <br>
   * - 다른 메서드에서 재사용하고, Transaction 보장하기 위해 public 메서드 사용
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month           생성할 개월 수
   * @return ResTimePrice
   */
  public List<ResPackagePrice> createPackagePrices(Long detailedSpaceId, int month) {

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
    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<PackagePrice> packagePrices = new ArrayList<>();
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
      packagePrices.add(price);
    }

    return SlotMapper.toResPackagePrice(packagePrices);
  }

  /**
   * <h1> 시간제, 패키지 슬롯 조회 </h1>
   *
   * @param detailedSpaceId 상세공간 ID
   * @param type            time, package 중 조회
   * @param month           조회할 개월 수 (기본 3개월)
   * @return ResPrice
   */
  public ResPrice getSlots(Long detailedSpaceId, String type, int month) {

    List<ResTimePrice> resTimePrices;
    List<ResPackagePrice> resPackagePrices;
    if (type.equals("time")) {
      resTimePrices = getTimeSlots(detailedSpaceId, month);
      return new ResPrice(resTimePrices, Collections.emptyList());
    }

    if (type.equals("package")) {
      resPackagePrices = getPackageSlots(detailedSpaceId, month);
      return new ResPrice(Collections.emptyList(), resPackagePrices);
    }

    throw new RuntimeException(String.format("유효하지 않은 타입입니다. %s", type));
  }

  /**
   * <h1> 슬롯 업데이트 </h1>
   *
   * <br>
   * - 가격 정보는 변경되었지만, 슬롯은 아직 변경되지 않은 상태<br> - 변경된 가격 정보 기준으로 슬롯 재생성, 동기화 여부에 따라 기존 예약 상태 반영
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month           생성할 개월 수
   * @return ResPrice {시간제 가격 정보, 패키지 가격 정보}
   */
  @Transactional
  public ResPrice updateSlots(Long detailedSpaceId, int month, boolean isSync) {

    List<ResTimePrice> resTimePrices = null;
    List<ResPackagePrice> resPackagePrices = null;
    List<Reservation> reservations = null;

    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<BasePriceInformation> timePriceInfoList = basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.TIME.getNum());

    if (isSync) {
      reservations = reservationRepository.findAllDetailedSpaceIdAndYearMonths(
        detailedSpaceId, targetYearMonths);
    }

    if (!timePriceInfoList.isEmpty()) {
      timePriceRepository.deleteTimePricesByDetailedSpaceIdAndYearMonth(detailedSpaceId,
        targetYearMonths);

      resTimePrices = createTimePrices(detailedSpaceId, month);
      if (isSync) {
        resTimePrices = syncTimeSlotReservationStatus(resTimePrices, reservations);
      }
    }

    List<BasePriceInformation> packagePriceInfoList = basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
      detailedSpaceId, PriceType.PACKAGE.getNum());

    if (!packagePriceInfoList.isEmpty()) {
      packagePriceRepository.deletePackagePricesByDetailedSpaceIdAndYearMonth(detailedSpaceId,
        getYearMonths(month));

      resPackagePrices = createPackagePrices(detailedSpaceId, month);
      if (isSync) {
        resPackagePrices = syncPackageSlotReservationStatus(resPackagePrices, reservations);
      }
    }

    return new ResPrice(resTimePrices, resPackagePrices);
  }

  private List<YearMonth> getYearMonths(int month) {

    return IntStream.rangeClosed(0, month)
      .mapToObj(YearMonth.now(clock)::plusMonths)
      .toList();
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
        curTimeBasePriceInformation.getPrice(), false);
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

    DayPackagePrice dayPackagePrice = new DayPackagePrice(null, curDay.getDayOfMonth(),
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
        LocalDateTime.of(endDay, priceInfo.getEndTime()), priceInfo.getPrice(), false);
      packageSlotRepository.save(packageSlotPrice);
      dayPackagePrices.add(packageSlotPrice);
    }
    return dayPackagePrices;
  }

  private List<ResTimePrice> getTimeSlots(Long detailedSpaceId, int month) {

    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<TimePrice> timePrices = timePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
      detailedSpaceId, targetYearMonths);

    return SlotMapper.toResTimePrice(timePrices);
  }

  private List<ResPackagePrice> getPackageSlots(Long detailedSpaceId, int month) {

    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<PackagePrice> packagePrices = packagePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
      detailedSpaceId, targetYearMonths);

    return SlotMapper.toResPackagePrice(packagePrices);
  }

  private List<ResTimePrice> syncTimeSlotReservationStatus(List<ResTimePrice> resTimePrices,
    List<Reservation> reservations) {

    List<Long> updateTimeSlotIds = getUpdateTimeSlotIds(resTimePrices, reservations);

    timeSlotRepository.updateIsReservedByIds(updateTimeSlotIds);

    return SlotMapper.toResTimePrice(resTimePrices, updateTimeSlotIds);
  }

  private List<Long> getUpdateTimeSlotIds(List<ResTimePrice> resTimePrices,
    List<Reservation> reservations) {

    List<Long> reservedTimeSlotIds = new ArrayList<>();
    for (var timePrice : resTimePrices) {

      String[] tokens = timePrice.yearAndMonth().split("-");
      int year = Integer.parseInt(tokens[0]);
      int month = Integer.parseInt(tokens[1]);

      for (var dayTimePrice : timePrice.dayTimePrices()) {

        int day = dayTimePrice.day();
        List<Reservation> filteredReservations = getFilteredReservations(
          reservations, year, month, day);

        for (var timeSlotPrice : dayTimePrice.timeSlotPrices()) {
          LocalTime slotStartTime = LocalTime.parse(timeSlotPrice.startTime());
          LocalTime slotEndTime = slotStartTime.plusMinutes(30);

          boolean isReserved = filteredReservations.stream()
            .anyMatch(reservation -> {
              LocalTime reservationStart = reservation.getStartTime().toLocalTime();
              LocalTime reservationEnd = reservation.getEndTime().toLocalTime();

              return slotStartTime.isBefore(reservationEnd) && slotEndTime.isAfter(
                reservationStart);
            });

          if (isReserved) {
            reservedTimeSlotIds.add(timeSlotPrice.id());
          }
        }
      }
    }
    return reservedTimeSlotIds;
  }

  private List<ResPackagePrice> syncPackageSlotReservationStatus(
    List<ResPackagePrice> resPackagePrices, List<Reservation> reservations) {

    List<Long> updatePackageSlotIds = getUpdatePackageSlotIds(resPackagePrices, reservations);

    packageSlotRepository.updateIsReservedByIds(updatePackageSlotIds);

    return SlotMapper.toResPackagePrice(resPackagePrices, updatePackageSlotIds);
  }

  private List<Long> getUpdatePackageSlotIds(List<ResPackagePrice> resPackagePrices,
    List<Reservation> reservations) {

    List<Long> reservedPackageSlotIds = new ArrayList<>();
    for (var packagePrice : resPackagePrices) {

      String[] tokens = packagePrice.yearAndMonth().split("-");
      int year = Integer.parseInt(tokens[0]);
      int month = Integer.parseInt(tokens[1]);

      for (var dayPackagePrice : packagePrice.dayPackagePrices()) {

        int day = dayPackagePrice.day();
        List<Reservation> filteredReservations = getFilteredReservations(
          reservations, year, month, day);

        for (var packageSlotPrice : dayPackagePrice.packageSlotPrices()) {
          LocalDateTime slotStartTime = LocalDateTime.parse(packageSlotPrice.startTime());
          LocalDateTime slotEndTime = LocalDateTime.parse(packageSlotPrice.endTime());

          boolean isReserved = filteredReservations.stream()
            .anyMatch(reservation -> {
              LocalDateTime reservationStart = reservation.getStartTime();
              LocalDateTime reservationEnd = reservation.getEndTime();

              return slotStartTime.isBefore(reservationEnd) && slotEndTime.isAfter(
                reservationStart);
            });

          if (isReserved) {
            reservedPackageSlotIds.add(packageSlotPrice.id());
          }
        }
      }
    }
    return reservedPackageSlotIds;
  }
}
