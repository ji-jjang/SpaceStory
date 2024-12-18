package com.juny.spacestory.domain.slot.repository;

import com.juny.spacestory.domain.slot.entity.PackageDayPrice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackageDayPriceRepository {

  void save(PackageDayPrice packageDayPrice);
}
