package com.juny.spacestory.domain.advertise.common.service;

import com.juny.spacestory.domain.advertise.common.entity.AdvertiseCoupon;
import com.juny.spacestory.domain.advertise.common.repository.AdvertiseCouponRepository;
import com.juny.spacestory.domain.point.common.service.PointService;
import com.juny.spacestory.domain.space.common.repository.SpaceRepository;
import com.juny.spacestory.domain.user.common.entity.User;
import com.juny.spacestory.domain.user.common.repository.UserRepository;
import com.juny.spacestory.global.constant.Constants;
import java.time.LocalDate;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdvertiseCouponService {

  public static HashMap<Integer, Integer> couponPriceMap = new HashMap<>();

  static {
    couponPriceMap.put(1, 20000);
    couponPriceMap.put(2, 30000);
    couponPriceMap.put(3, 40000);
  }

  private final AdvertiseCouponRepository advertiseCouponRepository;
  private final SpaceRepository spaceRepository;
  private final UserRepository userRepository;
  private final PointService pointService;

  /**
   *
   *
   * <h1>광고 쿠폰 발행 </h1>
   *
   * @param month 쿠폰 발행할 월 수
   * @return ResAdvertiseCoupon
   */
  @Transactional
  public AdvertiseCoupon createAdvertiseCoupon(int month, Long userId) {

    if (!couponPriceMap.containsKey(month)) {
      throw new RuntimeException("invalid coupon issue month");
    }

    int couponPrice = couponPriceMap.get(month);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException(String.format("invalid user id %d", userId)));

    if (user.getCurrentPoint() < couponPrice) {
      throw new RuntimeException(
          String.format(
              "user not enough point: %d, payAmount: %d", user.getCurrentPoint(), couponPrice));
    }

    pointService.processPointPayment(
        -couponPrice, user, null, Constants.POINT_BUY_ADVERTISE_COUPON_REASON);

    AdvertiseCoupon coupon =
        AdvertiseCoupon.builder()
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(month))
            .price(couponPriceMap.get(month))
            .build();

    advertiseCouponRepository.save(coupon);

    return coupon;
  }

  /**
   *
   *
   * <h1>광고 쿠폰 공간에 부착, 해제 </h1>
   *
   * <br>
   * - 인자로 주어진 상태에 따라 공간 is_advertised 컬럼 변경<br>
   * - 쿠폰 공간 부착하면 쿠폰은 공간 아이디를 참조, 해제하면 NULL 가리키도록
   *
   * @param spaceId 공간 아이디
   * @param advertiseCouponId 광고 쿠폰 아이디
   * @param advertiseStatus 변경할 광고 상태값
   */
  @Transactional
  public void updateAdvertiseCoupon(Long spaceId, Long advertiseCouponId, Boolean advertiseStatus) {

    AdvertiseCoupon coupon =
        advertiseCouponRepository
            .findById(advertiseCouponId)
            .orElseThrow(() -> new RuntimeException("유효하지 않은 쿠폰입니다."));

    if (coupon.getEndDate().isBefore(LocalDate.now())) {
      throw new RuntimeException("쿠폰 유효기간이 만료되었습니다.");
    }

    spaceRepository.updateSpaceAdvertising(spaceId, advertiseStatus);
    if (advertiseStatus) {
      advertiseCouponRepository.attachCouponToSpace(advertiseCouponId, spaceId);
      return;
    }
    advertiseCouponRepository.detachCouponToSpace(advertiseCouponId);
  }
}
