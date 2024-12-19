package com.juny.spacestory.domain.slot.service;

import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.repository.ReservationRepository;
import com.juny.spacestory.domain.slot.dto.PriceInfo;
import com.juny.spacestory.domain.slot.dto.ResPackagePrice;
import com.juny.spacestory.domain.slot.dto.ResPrice;
import com.juny.spacestory.domain.slot.dto.ResTimePrice;
import com.juny.spacestory.domain.slot.entity.BasePriceInformation;
import com.juny.spacestory.domain.slot.entity.DayType;
import com.juny.spacestory.domain.slot.entity.ExceptionPriceInformation;
import com.juny.spacestory.domain.slot.entity.PackageDayPrice;
import com.juny.spacestory.domain.slot.entity.PackagePrice;
import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeDayPrice;
import com.juny.spacestory.domain.slot.entity.TimePrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import com.juny.spacestory.domain.slot.mapper.SlotMapper;
import com.juny.spacestory.domain.slot.repository.BasePriceInformationRepository;
import com.juny.spacestory.domain.slot.repository.ExceptionPriceInformationRepository;
import com.juny.spacestory.domain.slot.repository.PackageDayPriceRepository;
import com.juny.spacestory.domain.slot.repository.PackagePriceRepository;
import com.juny.spacestory.domain.slot.repository.PackageSlotPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeDayPriceRepository;
import com.juny.spacestory.domain.slot.repository.TimePriceRepository;
import com.juny.spacestory.domain.slot.repository.TimeSlotPriceRepository;
import com.juny.spacestory.domain.space.entity.Space;
import com.juny.spacestory.domain.space.repository.SpaceRepository;
import com.juny.spacestory.global.constant.Constants;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
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

  private final TimeDayPriceRepository timeDayPriceRepository;

  private final TimeSlotPriceRepository timeSlotPriceRepository;

  private final PackagePriceRepository packagePriceRepository;

  private final PackageDayPriceRepository packageDayPriceRepository;

  private final PackageSlotPriceRepository packageSlotPriceRepository;

  private final ReservationRepository reservationRepository;

  private final Clock clock;

  private static List<Reservation> getFilteredReservations(
      List<Reservation> reservations, int year, Month month, int day) {

    return reservations.stream()
        .filter(
            reservation -> {
              LocalDate reservationDate = reservation.getStartDateTime().toLocalDate();
              return reservationDate.getYear() == year
                  && reservationDate.getMonth() == month
                  && reservationDate.getDayOfMonth() == day;
            })
        .toList();
  }

  /**
   *
   *
   * <h1>시간제, 패키지 슬롯 생성 </h1>
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month 생성할 개월 수 (기본 3개월)
   * @return ResPrice
   */
  @Transactional
  public PriceInfo createSlots(Long detailedSpaceId, int month) {

    List<TimePrice> timePrices = null;
    List<PackagePrice> packagePrices = null;

    List<BasePriceInformation> timePriceInfoList =
        basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME);

    if (!timePriceInfoList.isEmpty()) {
      timePrices = createTimePrices(detailedSpaceId, month);
    }

    List<BasePriceInformation> packagePriceInfoList =
        basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE);

    if (!packagePriceInfoList.isEmpty()) {
      packagePrices = createPackagePrices(detailedSpaceId, month);
    }

    return new PriceInfo(timePrices, packagePrices, null, null);
  }

  /**
   *
   *
   * <h1>시간제 슬롯 가격 생성 </h1>
   *
   * <br>
   * - 다른 메서드에서 재사용하고, Transaction 보장하기 위해 public 메서드 사용
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month 생성할 개월 수
   * @return ResTimePrice
   */
  public List<TimePrice> createTimePrices(Long detailedSpaceId, int month) {

    Space space =
        spaceRepository
            .findByDetailedSpaceId(detailedSpaceId)
            .orElseThrow(() -> new RuntimeException("유효한 상세 공간 ID가 아닙니다."));

    List<BasePriceInformation> basePriceInformationList =
        basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME);
    if (basePriceInformationList == null || basePriceInformationList.isEmpty()) {
      throw new RuntimeException("시간제 가격 정보가 없습니다.");
    }

    List<ExceptionPriceInformation> exceptionPriceInfoList =
        exceptionPriceInfoRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME);
    List<YearMonth> targetYearMonths = getYearMonths(month);
    LocalTime openingTime = space.getOpeningTime();
    LocalTime closingTime = space.getClosingTime();
    List<TimePrice> timePrices = new ArrayList<>();
    for (var yearMonth : targetYearMonths) {

      if (!timePriceRepository.existDetailedSpaceIdAndYearMonth(detailedSpaceId, yearMonth)) {
        continue;
      }

      List<ExceptionPriceInformation> targetExceptionPriceInfoList =
          exceptionPriceInfoList.stream()
              .filter(info -> YearMonth.from(info.getStartDate()).equals(yearMonth))
              .toList();

      List<TimeDayPrice> timeDayPrices =
          createDayTimePrices(
              yearMonth,
              openingTime,
              closingTime,
              basePriceInformationList,
              targetExceptionPriceInfoList);

      TimePrice price = new TimePrice(null, yearMonth, timeDayPrices);
      timePriceRepository.save(price);
      timePrices.add(price);
    }

    return timePrices;
  }

  /**
   *
   *
   * <h1>패키지 슬롯 가격 생성 </h1>
   *
   * <br>
   * - 다른 메서드에서 재사용하고, Transaction 보장하기 위해 public 메서드 사용
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month 생성할 개월 수
   * @return ResTimePrice
   */
  public List<PackagePrice> createPackagePrices(Long detailedSpaceId, int month) {

    List<BasePriceInformation> priceInfoList =
        basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE);

    if (priceInfoList == null || priceInfoList.isEmpty()) {
      throw new RuntimeException("패키지 가격 정보가 없습니다.");
    }

    List<BasePriceInformation> packagePriceInfoList =
        priceInfoList.stream()
            .filter(info -> info.getPriceType().equals(Constants.PRICE_TYPE_PACKAGE))
            .toList();

    List<ExceptionPriceInformation> exceptionPriceInfoList =
        exceptionPriceInfoRepository.findAllWithDetailsByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE);
    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<PackagePrice> packagePrices = new ArrayList<>();
    for (var yearMonth : targetYearMonths) {

      if (!packagePriceRepository.existDetailedSpaceIdAndYearMonth(detailedSpaceId, yearMonth)) {
        continue;
      }

      List<ExceptionPriceInformation> targetExceptionPriceInfoList =
          exceptionPriceInfoList.stream()
              .filter(info -> YearMonth.from(info.getStartDate()).equals(yearMonth))
              .toList();

      List<PackageDayPrice> packageDayPrices =
          createDayPackagePrices(yearMonth, packagePriceInfoList, targetExceptionPriceInfoList);

      PackagePrice price = new PackagePrice(null, yearMonth, packageDayPrices);
      packagePriceRepository.save(price);
      packagePrices.add(price);
    }

    return packagePrices;
  }

  /**
   *
   *
   * <h1>시간제, 패키지 슬롯 조회 </h1>
   *
   * @param detailedSpaceId 상세공간 ID
   * @param type time, package 중 조회
   * @param month 조회할 개월 수 (기본 3개월)
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
   *
   *
   * <h1>슬롯 업데이트 </h1>
   *
   * <br>
   * - 가격 정보는 변경되었지만, 슬롯은 아직 변경되지 않은 상태<br>
   * - 변경된 가격 정보 기준으로 슬롯 재생성, 동기화 여부에 따라 기존 예약 상태 반영
   *
   * @param detailedSpaceId 상세공간 ID
   * @param month 생성할 개월 수
   * @return ResPrice {시간제 가격 정보, 패키지 가격 정보}
   */
  @Transactional
  public PriceInfo updateSlots(Long detailedSpaceId, int month, boolean isSync) {

    List<TimePrice> timePrices = null;
    List<PackagePrice> packagePrices = null;
    List<Reservation> reservations = null;
    List<Long> updatedTimeSlotIds = null;
    List<Long> updatedPackageSlotIds = null;

    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<BasePriceInformation> timePriceInfoList =
        basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_TIME);

    if (isSync) {
      reservations =
          reservationRepository.findAllDetailedSpaceIdAndYearMonths(
              detailedSpaceId, targetYearMonths);
    }

    if (!timePriceInfoList.isEmpty()) {
      timePriceRepository.deleteTimePricesByDetailedSpaceIdAndYearMonth(
          detailedSpaceId, targetYearMonths);

      timePrices = createTimePrices(detailedSpaceId, month);
      if (isSync) {
        updatedTimeSlotIds = syncTimeSlotReservationStatus(timePrices, reservations);
      }
    }

    List<BasePriceInformation> packagePriceInfoList =
        basePriceInfoRepository.findByDetailedSpaceIdAndPriceType(
            detailedSpaceId, Constants.PRICE_TYPE_PACKAGE);

    if (!packagePriceInfoList.isEmpty()) {
      packagePriceRepository.deletePackagePricesByDetailedSpaceIdAndYearMonth(
          detailedSpaceId, getYearMonths(month));

      packagePrices = createPackagePrices(detailedSpaceId, month);
      if (isSync) {
        updatedPackageSlotIds = syncPackageSlotReservationStatus(packagePrices, reservations);
      }
    }

    return new PriceInfo(timePrices, packagePrices, updatedTimeSlotIds, updatedPackageSlotIds);
  }

  private List<YearMonth> getYearMonths(int month) {

    return IntStream.rangeClosed(0, month).mapToObj(YearMonth.now(clock)::plusMonths).toList();
  }

  private List<TimeDayPrice> createDayTimePrices(
      YearMonth yearMonth,
      LocalTime openingTime,
      LocalTime closingTime,
      List<BasePriceInformation> basePriceInfoList,
      List<ExceptionPriceInformation> exceptionPriceInfoList) {

    List<TimeDayPrice> timeDayPrices = new ArrayList<>();
    for (var exceptionPriceInfo : exceptionPriceInfoList) {

      LocalDate curDay = exceptionPriceInfo.getStartDate();
      LocalDate endDay = exceptionPriceInfo.getEndDate();
      while (!curDay.isAfter(endDay)) {

        createDayTimePrice(
            openingTime,
            closingTime,
            exceptionPriceInfo.getExceptionPriceDetails(),
            curDay,
            timeDayPrices);
        curDay = curDay.plusDays(1);
      }
    }

    List<LocalDate> basePriceDate = getBasePriceDate(yearMonth, exceptionPriceInfoList);
    for (var date : basePriceDate) {

      createDayTimePrice(openingTime, closingTime, basePriceInfoList, date, timeDayPrices);
    }

    return timeDayPrices;
  }

  private List<LocalDate> getBasePriceDate(
      YearMonth yearMonth, List<ExceptionPriceInformation> exceptionPriceInfoList) {

    return IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
        .filter(
            day -> {
              LocalDate date = yearMonth.atDay(day);
              return !date.isBefore(LocalDate.now(clock))
                  && exceptionPriceInfoList.stream()
                      .noneMatch(
                          info -> isDateInRange(day, info.getStartDate(), info.getEndDate()));
            })
        .boxed()
        .sorted()
        .map(yearMonth::atDay)
        .toList();
  }

  private boolean isDateInRange(int day, LocalDate startDate, LocalDate endDate) {
    return day >= startDate.getDayOfMonth() && day <= endDate.getDayOfMonth();
  }

  private void createDayTimePrice(
      LocalTime openingTime,
      LocalTime closingTime,
      List<? extends com.juny.spacestory.domain.slot.entity.PriceInfo> priceInfoList,
      LocalDate curDay,
      List<TimeDayPrice> timeDayPrices) {

    List<TimeSlotPrice> timeSlotPrices =
        createTimeSlotPrices(
            DayType.fromLocalDate(curDay), openingTime, closingTime, priceInfoList);

    TimeDayPrice timeDayPrice =
        new TimeDayPrice(null, curDay.getDayOfMonth(), false, timeSlotPrices);
    timeDayPriceRepository.save(timeDayPrice);
    timeDayPrices.add(timeDayPrice);
  }

  private List<TimeSlotPrice> createTimeSlotPrices(
      DayType curDayType,
      LocalTime openingTime,
      LocalTime closingTime,
      List<? extends com.juny.spacestory.domain.slot.entity.PriceInfo> basePriceInformationList) {

    List<TimeSlotPrice> timeSlotPrices = new ArrayList<>();
    LocalTime curTime = openingTime;

    while (curTime.isBefore(closingTime)) {

      LocalTime finalCurTime = curTime;

      com.juny.spacestory.domain.slot.entity.PriceInfo curTimeBasePriceInformation =
          basePriceInformationList.stream()
              .filter(
                  info ->
                      info.getDayType() == curDayType.getNum()
                          && info.getStartTime().equals(finalCurTime))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("해당 시간에 해당하는 시간제 가격 정보가 없습니다."));

      TimeSlotPrice timeSlotPrice =
          new TimeSlotPrice(null, curTime, curTimeBasePriceInformation.getPrice(), false);
      timeSlotPriceRepository.save(timeSlotPrice);

      timeSlotPrices.add(timeSlotPrice);
      curTime = curTime.plusMinutes(30);
    }

    return timeSlotPrices;
  }

  private List<PackageDayPrice> createDayPackagePrices(
      YearMonth yearMonth,
      List<BasePriceInformation> packagePriceInfoList,
      List<ExceptionPriceInformation> exceptionPriceInfoList) {

    List<PackageDayPrice> packageDayPrices = new ArrayList<>();
    for (var exceptionPriceInfo : exceptionPriceInfoList) {

      LocalDate curDay = exceptionPriceInfo.getStartDate();
      LocalDate endDay = exceptionPriceInfo.getEndDate();
      while (!curDay.isAfter(endDay)) {

        createDayPackagePrice(
            exceptionPriceInfo.getExceptionPriceDetails(), curDay, packageDayPrices);
        curDay = curDay.plusDays(1);
      }
    }

    List<LocalDate> basePriceDate = getBasePriceDate(yearMonth, exceptionPriceInfoList);
    for (var date : basePriceDate) {

      createDayPackagePrice(packagePriceInfoList, date, packageDayPrices);
    }

    return packageDayPrices;
  }

  private void createDayPackagePrice(
      List<? extends com.juny.spacestory.domain.slot.entity.PriceInfo> exceptionPriceDetails,
      LocalDate curDay,
      List<PackageDayPrice> packageDayPrices) {

    List<PackageSlotPrice> packageSlotPrices =
        createPackageSlotPrices(exceptionPriceDetails, curDay);

    PackageDayPrice packageDayPrice =
        new PackageDayPrice(null, curDay.getDayOfMonth(), packageSlotPrices);
    packageDayPriceRepository.save(packageDayPrice);
    packageDayPrices.add(packageDayPrice);
  }

  private List<PackageSlotPrice> createPackageSlotPrices(
      List<? extends com.juny.spacestory.domain.slot.entity.PriceInfo> packagePriceInfoList,
      LocalDate curDay) {

    List<PackageSlotPrice> dayPackagePrices = new ArrayList<>();
    LocalDate endDay = curDay;

    List<? extends com.juny.spacestory.domain.slot.entity.PriceInfo> list =
        packagePriceInfoList.stream()
            .filter(info -> info.getDayType().equals(DayType.fromLocalDate(curDay).getNum()))
            .toList();

    for (var priceInfo : list) {
      if (!priceInfo.getStartTime().isBefore(priceInfo.getEndTime())) {
        endDay = curDay.plusDays(1);
      }
      PackageSlotPrice packageSlotPrice =
          new PackageSlotPrice(
              null,
              priceInfo.getName(),
              LocalDateTime.of(curDay, priceInfo.getStartTime()),
              LocalDateTime.of(endDay, priceInfo.getEndTime()),
              priceInfo.getPrice(),
              false);
      packageSlotPriceRepository.save(packageSlotPrice);
      dayPackagePrices.add(packageSlotPrice);
    }
    return dayPackagePrices;
  }

  private List<ResTimePrice> getTimeSlots(Long detailedSpaceId, int month) {

    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<TimePrice> timePrices =
        timePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
            detailedSpaceId, targetYearMonths);

    return SlotMapper.toResTimePrice(timePrices);
  }

  private List<ResPackagePrice> getPackageSlots(Long detailedSpaceId, int month) {

    List<YearMonth> targetYearMonths = getYearMonths(month);

    List<PackagePrice> packagePrices =
        packagePriceRepository.findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
            detailedSpaceId, targetYearMonths);

    return SlotMapper.toResPackagePrice(packagePrices);
  }

  private List<Long> syncTimeSlotReservationStatus(
      List<TimePrice> timePrices, List<Reservation> reservations) {

    List<Long> updateTimeSlotIds = getUpdateTimeSlotIds(timePrices, reservations);

    timeSlotPriceRepository.updateIsReservedByIds(updateTimeSlotIds);

    return updateTimeSlotIds;
  }

  private List<Long> getUpdateTimeSlotIds(
      List<TimePrice> timePrices, List<Reservation> reservations) {

    List<Long> reservedTimeSlotIds = new ArrayList<>();
    for (var timePrice : timePrices) {

      YearMonth yearAndMonth = timePrice.getYearAndMonth();
      for (var dayTimePrice : timePrice.getTimeDayPrices()) {

        int day = dayTimePrice.getDay();
        List<Reservation> filteredReservations =
            getFilteredReservations(
                reservations, yearAndMonth.getYear(), yearAndMonth.getMonth(), day);

        for (var timeSlotPrice : dayTimePrice.getTimeSlotPrices()) {

          LocalTime slotStartTime = timeSlotPrice.getStartTime();
          LocalTime slotEndTime = slotStartTime.plusMinutes(30);

          boolean isReserved =
              filteredReservations.stream()
                  .anyMatch(
                      reservation -> {
                        LocalTime reservationStart = reservation.getStartDateTime().toLocalTime();
                        LocalTime reservationEnd = reservation.getEndDateTime().toLocalTime();

                        return slotStartTime.isBefore(reservationEnd)
                            && slotEndTime.isAfter(reservationStart);
                      });

          if (isReserved) {
            reservedTimeSlotIds.add(timeSlotPrice.getId());
          }
        }
      }
    }
    return reservedTimeSlotIds;
  }

  private List<Long> syncPackageSlotReservationStatus(
      List<PackagePrice> packagePrices, List<Reservation> reservations) {

    List<Long> updatePackageSlotIds = getUpdatePackageSlotIds(packagePrices, reservations);

    packageSlotPriceRepository.updateIsReservedByIds(updatePackageSlotIds);

    return updatePackageSlotIds;
  }

  private List<Long> getUpdatePackageSlotIds(
      List<PackagePrice> packagePrices, List<Reservation> reservations) {

    List<Long> reservedPackageSlotIds = new ArrayList<>();
    for (var packagePrice : packagePrices) {

      YearMonth yearAndMonth = packagePrice.getYearAndMonth();
      for (var dayPackagePrice : packagePrice.getPackageDayPrices()) {

        int day = dayPackagePrice.getDay();
        List<Reservation> filteredReservations =
            getFilteredReservations(
                reservations, yearAndMonth.getYear(), yearAndMonth.getMonth(), day);

        for (var packageSlotPrice : dayPackagePrice.getPackageSlotPrices()) {
          LocalDateTime slotStartTime = packageSlotPrice.getStartTime();
          LocalDateTime slotEndTime = packageSlotPrice.getEndTime();

          boolean isReserved =
              filteredReservations.stream()
                  .anyMatch(
                      reservation -> {
                        LocalDateTime reservationStart = reservation.getStartDateTime();
                        LocalDateTime reservationEnd = reservation.getEndDateTime();

                        return slotStartTime.isBefore(reservationEnd)
                            && slotEndTime.isAfter(reservationStart);
                      });

          if (isReserved) {
            reservedPackageSlotIds.add(packageSlotPrice.getId());
          }
        }
      }
    }
    return reservedPackageSlotIds;
  }
}
