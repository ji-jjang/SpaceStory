package com.juny.spacestory.domain.slot.repository;

import com.juny.spacestory.domain.slot.entity.TimePrice;
import java.time.YearMonth;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimePriceRepository {

  void save(TimePrice price);

  Boolean existDetailedSpaceIdAndYearMonth(Long detailedSpaceId, YearMonth yearMonth);

  List<TimePrice> findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
      Long detailedSpaceId, List<YearMonth> targetYearMonths);

  void deleteTimePricesByDetailedSpaceIdAndYearMonth(
      Long detailedSpaceId, List<YearMonth> yearMonths);
}
