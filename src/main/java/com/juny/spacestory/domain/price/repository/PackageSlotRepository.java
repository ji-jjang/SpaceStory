package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.PackageSlotPrice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackageSlotRepository {

  void save(PackageSlotPrice packageSlotPrice);
}
