package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.BasePriceInformation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BasePriceInformationRepository {

  List<BasePriceInformation> findByDetailedSpaceIdAndPriceType(Long detailedSpaceId,
    Integer priceType);
}
