package com.juny.spacestory.domain.reservation.service;

import com.juny.spacestory.domain.price.entity.PackageSlotPrice;
import com.juny.spacestory.domain.price.entity.TimeSlotPrice;
import com.juny.spacestory.domain.reservation.entity.ReservationPriceInfo;
import com.juny.spacestory.domain.reservation.repository.ReservationPriceInfoRepository;
import com.juny.spacestory.global.constant.Constants;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationPriceCalculateService {

  private final ReservationPriceInfoRepository reservationPriceInfoRepository;

  /**
   *
   *
   * <h1>예약 가격 계산 </h1>
   *
   * <br>
   * - 시간제 가격은 사람당 시간 요금인지, 공간당 요금인지에 따라 계산 로직 다름.<br>
   * - 전자인 경우 전체 슬롯 가격 X 인원 수, 후자인 경우 슬롯 가격 + (추가 인원 요금 X 추가 인원)<br>
   * - 패키지 가격은 패키지 슬롯 가격 + (추가 인원 요금 X 추가 인원)
   *
   * @param priceType String {time, package}
   * @param guestCount 손님 수
   * @param timeSlotPrices 시간 슬롯
   * @param packageSlotPrice 패키지 슬롯
   * @param detailedSpaceId 상세공간 ID
   * @return 예약 가격 총합
   */
  public int calculateTotalPrice(
      String priceType,
      Integer guestCount,
      List<TimeSlotPrice> timeSlotPrices,
      PackageSlotPrice packageSlotPrice,
      Long detailedSpaceId) {

    ReservationPriceInfo reservationPriceInfo =
        reservationPriceInfoRepository
            .findByDetailedSpaceIdAndPriceType(detailedSpaceId, priceType)
            .orElseThrow(() -> new RuntimeException("reservation price info not found"));

    if (priceType.equals(Constants.PRICE_TYPE_TIME)) {
      return calculateTotalTimeSlotPrice(guestCount, timeSlotPrices, reservationPriceInfo);
    }
    return calculateTotalPackageSlotPrice(guestCount, packageSlotPrice, reservationPriceInfo);
  }

  private int calculateTotalTimeSlotPrice(
      Integer guestCount,
      List<TimeSlotPrice> timeSlotPrices,
      ReservationPriceInfo reservationPriceInfo) {

    int slotTotalPrice = 0;
    for (var timeSlot : timeSlotPrices) {
      slotTotalPrice += timeSlot.getPrice();
    }

    int additionalGuestCount =
        Math.max((guestCount - reservationPriceInfo.getStandardCapacity()), 0);

    if (reservationPriceInfo.getIsPerPersonRate()) {
      return slotTotalPrice * guestCount;
    }

    return slotTotalPrice
        + additionalGuestCount * reservationPriceInfo.getPerPersonAdditionalRate();
  }

  private int calculateTotalPackageSlotPrice(
      Integer guestCount,
      PackageSlotPrice packageSlotPrice,
      ReservationPriceInfo reservationPriceInfo) {

    int additionalGuestCount =
        Math.max((guestCount - reservationPriceInfo.getStandardCapacity()), 0);

    return packageSlotPrice.getPrice()
        + additionalGuestCount * reservationPriceInfo.getPerPersonAdditionalRate();
  }
}
