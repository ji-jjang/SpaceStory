package com.juny.spacestory.domain.slot.repository;

import com.juny.spacestory.domain.slot.entity.TimeDayPrice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TimeDayPriceRepository {

  void save(TimeDayPrice timeDayPrice);
}
