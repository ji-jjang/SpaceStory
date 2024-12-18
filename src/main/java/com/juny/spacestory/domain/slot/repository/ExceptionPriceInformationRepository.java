package com.juny.spacestory.domain.slot.repository;

import com.juny.spacestory.domain.slot.entity.ExceptionPriceInformation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExceptionPriceInformationRepository {

  List<ExceptionPriceInformation> findAllWithDetailsByDetailedSpaceIdAndPriceType(
      Long detailedSpaceId, String priceType);
}
