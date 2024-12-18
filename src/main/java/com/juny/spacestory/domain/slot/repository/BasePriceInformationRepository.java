package com.juny.spacestory.domain.slot.repository;

import com.juny.spacestory.domain.slot.entity.BasePriceInformation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BasePriceInformationRepository {

  List<BasePriceInformation> findByDetailedSpaceIdAndPriceType(
      Long detailedSpaceId, String priceType);
}
