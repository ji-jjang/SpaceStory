package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.PackagePrice;
import java.time.YearMonth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackagePriceRepository {

  void save(PackagePrice price);

  Boolean existDetailedSpaceIdAndYearMonth(Long detailedSpaceId, YearMonth yearMonth);
}
