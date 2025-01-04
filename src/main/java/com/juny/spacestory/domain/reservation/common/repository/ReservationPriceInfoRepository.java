package com.juny.spacestory.domain.reservation.common.repository;

import com.juny.spacestory.domain.reservation.common.entity.ReservationPriceInfo;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationPriceInfoRepository {

  Optional<ReservationPriceInfo> findByDetailedSpaceIdAndPriceType(
      Long detailedSpaceId, String priceType);
}
