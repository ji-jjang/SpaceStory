package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.PackagePrice;
import java.time.YearMonth;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackagePriceRepository {

  void save(PackagePrice price);

  Boolean existDetailedSpaceIdAndYearMonth(Long detailedSpaceId, YearMonth yearMonth);

  List<PackagePrice> findAllByDetailedSpaceIdOrderByYearAndMonthAsc(
      Long detailedSpaceId, List<YearMonth> targetYearMonths);

  void deletePackagePricesByDetailedSpaceIdAndYearMonth(
      Long detailedSpaceId, List<YearMonth> yearMonths);
}
