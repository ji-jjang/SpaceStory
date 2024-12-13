package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.PackageSlotPrice;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackageSlotRepository {

  void save(PackageSlotPrice packageSlotPrice);

  void updateIsReservedByIds(List<Long> updatePackageSlotIds);
}
