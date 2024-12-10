package com.juny.spacestory.domain.price.entity;

import java.time.LocalTime;

public interface PriceInfo {

  Long getId();

  Integer getPriceType();

  Integer getDayType();

  LocalTime getStartTime();

  LocalTime getEndTime();

  String getName();

  Integer getPrice();
}
