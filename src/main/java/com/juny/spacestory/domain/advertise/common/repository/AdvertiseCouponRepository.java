package com.juny.spacestory.domain.advertise.common.repository;

import com.juny.spacestory.domain.advertise.common.entity.AdvertiseCoupon;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdvertiseCouponRepository {

  void save(AdvertiseCoupon advertiseCoupon);

  Optional<AdvertiseCoupon> findById(Long id);

  void attachCouponToSpace(Long id, Long spaceId);

  void detachCouponToSpace(Long id);
}
