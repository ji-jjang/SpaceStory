package com.juny.spacestory.domain.price.repository;

import com.juny.spacestory.domain.price.entity.DayTimePrice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DayTimeRepository {

  void save(DayTimePrice dayTimePrice);
}
