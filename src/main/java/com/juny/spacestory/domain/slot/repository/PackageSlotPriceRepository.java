package com.juny.spacestory.domain.slot.repository;

import com.juny.spacestory.domain.slot.entity.PackageSlotPrice;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PackageSlotPriceRepository {

  void save(PackageSlotPrice packageSlotPrice);

  void updateIsReservedByIds(List<Long> ids);

  void updateIsReservedById(Long id);

  Optional<PackageSlotPrice> findByIdForUpdate(Long id);
}
