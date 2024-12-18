package com.juny.spacestory.domain.slot.entity;

import java.time.LocalTime;

public interface PriceInfo {

  Long getId();

  String getPriceType();

  Integer getDayType();

  LocalTime getStartTime();

  LocalTime getEndTime();

  String getName();

  Integer getPrice();
}
