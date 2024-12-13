package com.juny.spacestory.domain.advertise.repository;

import com.juny.spacestory.domain.advertise.entity.AdvertiseCoupon;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdvertiseCouponRepository {

  AdvertiseCoupon save(AdvertiseCoupon advertiseCoupon);

  Optional<AdvertiseCoupon> findById(Long id);

  void attachCouponToSpace(Long id, Long spaceId);

  void detachCouponToSpace(Long id);
}
