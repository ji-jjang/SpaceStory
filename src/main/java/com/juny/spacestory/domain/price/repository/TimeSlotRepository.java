package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.TimeSlotPrice;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimeSlotRepository {

  void save(TimeSlotPrice timeSlotPrice);

  void updateIsReservedByIds(List<Long> updateIds);
}
