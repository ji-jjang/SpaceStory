package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.DayPackagePrice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DayPackageRepository {

  void save(DayPackagePrice dayPackagePrice);
}
