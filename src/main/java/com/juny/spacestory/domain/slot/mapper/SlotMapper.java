package com.juny.spacestory.domain.slot.mapper;

import com.juny.spacestory.domain.slot.dto.ResDayPackagePrice;
import com.juny.spacestory.domain.slot.dto.ResDayTimePrice;
import com.juny.spacestory.domain.slot.dto.ResPackagePrice;
import com.juny.spacestory.domain.slot.dto.ResPackageSlotPrice;
import com.juny.spacestory.domain.slot.dto.ResTimePrice;
import com.juny.spacestory.domain.slot.dto.ResTimeSlotPrice;
import com.juny.spacestory.domain.slot.entity.PackageDayPrice;
import com.juny.spacestory.domain.slot.entity.PackagePrice;
import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import com.juny.spacestory.domain.slot.entity.TimeDayPrice;
import com.juny.spacestory.domain.slot.entity.TimePrice;
import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import java.util.Collections;
import java.util.List;

public class SlotMapper {

  /**
   *
   *
   * <h1>TimePrice 계층형 응답 반환 </h1>
   *
   * @param timePrices 연월 시간 가격 정보 -> 일자 가격 정보 -> 하루 타임 슬롯 가격 정보
   * @return ResTimePrice
   */
  public static List<ResTimePrice> toResTimePrice(List<TimePrice> timePrices) {

    return timePrices.stream()
        .map(
            timePrice ->
                new ResTimePrice(
                    timePrice.getYearAndMonth().toString(),
                    toResDayTimePrice(timePrice.getTimeDayPrices())))
        .toList();
  }

  /**
   *
   *
   * <h1>TimePackage 계층형 응답 반환 </h1>
   *
   * @param packagePrices 연월 패키지 가격 정보 -> 일자 가격 정보 -> 하루 패키지 슬롯 가격 정보
   * @return ResPackagePrice
   */
  public static List<ResPackagePrice> toResPackagePrice(List<PackagePrice> packagePrices) {

    return packagePrices.stream()
        .map(
            packagePrice ->
                new ResPackagePrice(
                    packagePrice.getYearAndMonth().toString(),
                    toResDayPackagePrice(packagePrice.getPackageDayPrices())))
        .toList();
  }

  private static List<ResDayTimePrice> toResDayTimePrice(List<TimeDayPrice> timeDayPrices) {

    return timeDayPrices.stream()
        .map(
            dayTimePrice -> {
              boolean isAllReserved = isAllTimeSlotsReserved(dayTimePrice.getTimeSlotPrices());

              return new ResDayTimePrice(
                  dayTimePrice.getDay(),
                  isAllReserved,
                  isAllReserved
                      ? Collections.emptyList()
                      : toResTimeSlotPrice(dayTimePrice.getTimeSlotPrices()));
            })
        .toList();
  }

  private static List<ResTimeSlotPrice> toResTimeSlotPrice(List<TimeSlotPrice> timeSlotPrices) {

    return timeSlotPrices.stream()
        .map(
            timeSlotPrice ->
                new ResTimeSlotPrice(
                    timeSlotPrice.getId(), timeSlotPrice.getStartTime().toString(),
                    timeSlotPrice.getPrice(), timeSlotPrice.getIsReserved()))
        .toList();
  }

  private static Boolean isAllTimeSlotsReserved(List<TimeSlotPrice> timeSlotPrices) {

    return timeSlotPrices.stream().allMatch(TimeSlotPrice::getIsReserved);
  }

  private static List<ResDayPackagePrice> toResDayPackagePrice(
      List<PackageDayPrice> packageDayPrices) {

    return packageDayPrices.stream()
        .map(
            dayPackagePrice -> {
              boolean isAllReserved =
                  isAllPackageSlotReserved(dayPackagePrice.getPackageSlotPrices());

              return new ResDayPackagePrice(
                  dayPackagePrice.getDay(),
                  isAllReserved,
                  isAllReserved
                      ? Collections.emptyList()
                      : toResPackageSlotPrice(dayPackagePrice.getPackageSlotPrices()));
            })
        .toList();
  }

  private static boolean isAllPackageSlotReserved(List<PackageSlotPrice> packageSlotPrices) {

    return packageSlotPrices.stream().allMatch(PackageSlotPrice::getIsReserved);
  }

  private static List<ResPackageSlotPrice> toResPackageSlotPrice(
      List<PackageSlotPrice> packageSlotPrices) {

    return packageSlotPrices.stream()
        .map(
            packageSlotPrice ->
                new ResPackageSlotPrice(
                    packageSlotPrice.getId(),
                    packageSlotPrice.getName(),
                    packageSlotPrice.getStartTime().toString(),
                    packageSlotPrice.getEndTime().toString(),
                    packageSlotPrice.getPrice(),
                    packageSlotPrice.getIsReserved()))
        .toList();
  }

  public static List<ResTimePrice> toTimePricesUpdateIds(
      List<TimePrice> timePrices, List<Long> updateIds) {

    List<TimePrice> timePriceList =
        timePrices.stream()
            .map(
                timePrice ->
                    timePrice.toBuilder()
                        .timeDayPrices(
                            timePrice.getTimeDayPrices().stream()
                                .map(
                                    dayTimePrice ->
                                        dayTimePrice.toBuilder()
                                            .timeSlotPrices(
                                                dayTimePrice.getTimeSlotPrices().stream()
                                                    .map(
                                                        timeSlotPrice ->
                                                            timeSlotPrice.toBuilder()
                                                                .isReserved(
                                                                    updateIds.contains(
                                                                        timeSlotPrice.getId()))
                                                                .build())
                                                    .toList())
                                            .build())
                                .toList())
                        .build())
            .toList();

    return toResTimePrice(timePriceList);
  }

  public static List<ResPackagePrice> toPackagePricesUpdateIds(
      List<PackagePrice> packagePrices, List<Long> updateIds) {

    List<PackagePrice> packagePriceList =
        packagePrices.stream()
            .map(
                packagePrice ->
                    packagePrice.toBuilder()
                        .packageDayPrices(
                            packagePrice.getPackageDayPrices().stream()
                                .map(
                                    dayPackagePrice ->
                                        dayPackagePrice.toBuilder()
                                            .packageSlotPrices(
                                                dayPackagePrice.getPackageSlotPrices().stream()
                                                    .map(
                                                        timeSlotPrice ->
                                                            timeSlotPrice.toBuilder()
                                                                .isReserved(
                                                                    updateIds.contains(
                                                                        timeSlotPrice.getId()))
                                                                .build())
                                                    .toList())
                                            .build())
                                .toList())
                        .build())
            .toList();

    return toResPackagePrice(packagePrices);
  }
}
