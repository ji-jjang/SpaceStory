package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.TimePrice;
import java.time.YearMonth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimePriceRepository {

  void save(TimePrice price);

  Boolean existDetailedSpaceIdAndYearMonth(Long detailedSpaceId, YearMonth yearMonth);
}
