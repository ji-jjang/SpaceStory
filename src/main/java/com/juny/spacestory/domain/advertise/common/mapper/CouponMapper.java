package com.juny.spacestory.domain.advertise.common.mapper;

import com.juny.spacestory.domain.advertise.common.dto.ResCreateAdvertiseCoupon;
import com.juny.spacestory.domain.advertise.common.entity.AdvertiseCoupon;

public class CouponMapper {

  public static ResCreateAdvertiseCoupon toResCreateCoupon(AdvertiseCoupon coupon) {

    return new ResCreateAdvertiseCoupon(
        coupon.getId(), coupon.getStartDate(), coupon.getEndDate(), coupon.getPrice(), -1L);
  }
}
