package com.juny.spacestory.domain.slot.repository;

import com.juny.spacestory.domain.slot.entity.TimeSlotPrice;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimeSlotPriceRepository {

  void save(TimeSlotPrice timeSlotPrice);

  void updateIsReservedByIds(List<Long> updateIds);

  List<TimeSlotPrice> findByIdsForUpdateOrderByStartTimeASC(List<Long> slotIds);
}
