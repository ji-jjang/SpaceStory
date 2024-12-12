package com.juny.spacestory.domain.price.mapper;

import com.juny.spacestory.domain.price.dto.ResDayPackagePrice;
import com.juny.spacestory.domain.price.dto.ResDayTimePrice;
import com.juny.spacestory.domain.price.dto.ResPackagePrice;
import com.juny.spacestory.domain.price.dto.ResPackageSlotPrice;
import com.juny.spacestory.domain.price.dto.ResTimePrice;
import com.juny.spacestory.domain.price.dto.ResTimeSlotPrice;
import com.juny.spacestory.domain.price.entity.DayPackagePrice;
import com.juny.spacestory.domain.price.entity.DayTimePrice;
import com.juny.spacestory.domain.price.entity.PackagePrice;
import com.juny.spacestory.domain.price.entity.PackageSlotPrice;
import com.juny.spacestory.domain.price.entity.TimePrice;
import com.juny.spacestory.domain.price.entity.TimeSlotPrice;
import java.util.Collections;
import java.util.List;

public class SlotMapper {

  /**
   * <h1> TimePrice 계층형 응답 반환 </h1>
   *
   * @param timePrices 연월 시간 가격 정보 -> 일자 가격 정보 -> 하루 타임 슬롯 가격 정보
   * @return ResTimePrice
   */
  public static List<ResTimePrice> toResTimePrice(List<TimePrice> timePrices) {

    return timePrices.stream()
      .map(timePrice -> new ResTimePrice(timePrice.getYearAndMonth().toString(),
        toResDayTimePrice(timePrice.getDayTimePrices())))
      .toList();
  }

  /**
   * <h1> TimePackage 계층형 응답 반환 </h1>
   *
   * @param packagePrices 연월 패키지 가격 정보 -> 일자 가격 정보 -> 하루 패키지 슬롯 가격 정보
   * @return ResPackagePrice
   */
  public static List<ResPackagePrice> toResPackagePrice(List<PackagePrice> packagePrices) {

    return packagePrices.stream()
      .map(packagePrice -> new ResPackagePrice(packagePrice.getYearAndMonth().toString(),
        toResDayPackagePrice(packagePrice.getDayPackagePrices())))
      .toList();
  }


  private static List<ResDayTimePrice> toResDayTimePrice(List<DayTimePrice> dayTimePrices) {

    return dayTimePrices.stream()
      .map(dayTimePrice -> {

        boolean isAllReserved = isAllTimeSlotsReserved(dayTimePrice.getTimeSlotPrices());

        return new ResDayTimePrice(dayTimePrice.getDay(),
          isAllReserved,
          isAllReserved ? Collections.emptyList()
            : toResTimeSlotPrice(dayTimePrice.getTimeSlotPrices()));
      })
      .toList();
  }

  private static List<ResTimeSlotPrice> toResTimeSlotPrice(List<TimeSlotPrice> timeSlotPrices) {

    return timeSlotPrices.stream()
      .map(timeSlotPrice -> new ResTimeSlotPrice(timeSlotPrice.getStartTime().toString(),
        timeSlotPrice.getPrice(), timeSlotPrice.getIsReserved()))
      .toList();
  }

  private static Boolean isAllTimeSlotsReserved(List<TimeSlotPrice> timeSlotPrices) {

    return timeSlotPrices.stream().allMatch(TimeSlotPrice::getIsReserved);
  }

  private static List<ResDayPackagePrice> toResDayPackagePrice(
    List<DayPackagePrice> dayPackagePrices) {

    return dayPackagePrices.stream().map(
      dayPackagePrice -> {

        boolean isAllReserved = isAllPackageSlotReserved(dayPackagePrice.getPackageSlotPrices());

        return new ResDayPackagePrice(
          dayPackagePrice.getDay(),
          isAllReserved,
          isAllReserved ? Collections.emptyList()
            : toResPackageSlotPrice(dayPackagePrice.getPackageSlotPrices())
        );
      }
    ).toList();
  }

  private static boolean isAllPackageSlotReserved(List<PackageSlotPrice> packageSlotPrices) {

    return packageSlotPrices.stream().allMatch(PackageSlotPrice::getIsReserved);
  }


  private static List<ResPackageSlotPrice> toResPackageSlotPrice(
    List<PackageSlotPrice> packageSlotPrices) {

    return packageSlotPrices.stream().map(
      packageSlotPrice -> new ResPackageSlotPrice(
        packageSlotPrice.getName(),
        packageSlotPrice.getStartTime().toString(),
        packageSlotPrice.getEndTime().toString(),
        packageSlotPrice.getPrice(),
        packageSlotPrice.getIsReserved()
      )
    ).toList();
  }
}
