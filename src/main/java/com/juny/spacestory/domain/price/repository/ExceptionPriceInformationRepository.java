package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.ExceptionPriceInformation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExceptionPriceInformationRepository {

  List<ExceptionPriceInformation> findAllWithDetailsByDetailedSpaceIdAndPriceType(
    Long detailedSpaceId, Integer priceType);
}
