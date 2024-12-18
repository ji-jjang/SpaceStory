package com.juny.spacestory.domain.slot.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public enum DayType {
  HOLIDAY(0),
  MONDAY(1),
  TUESDAY(2),
  WEDNESDAY(3),
  THURSDAY(4),
  FRIDAY(5),
  SATURDAY(6),
  SUNDAY(7);

  private final int num;

  DayType(int num) {
    this.num = num;
  }

  public static DayType fromLocalDate(LocalDate date) {

    if (Holiday.holidays.contains(date)) {
      return HOLIDAY;
    }

    DayOfWeek dayOfWeek = date.getDayOfWeek();
    if (dayOfWeek == DayOfWeek.SUNDAY) {
      return SUNDAY;
    }
    if (dayOfWeek == DayOfWeek.MONDAY) {
      return MONDAY;
    }
    if (dayOfWeek == DayOfWeek.TUESDAY) {
      return TUESDAY;
    }
    if (dayOfWeek == DayOfWeek.WEDNESDAY) {
      return WEDNESDAY;
    }
    if (dayOfWeek == DayOfWeek.THURSDAY) {
      return THURSDAY;
    }
    if (dayOfWeek == DayOfWeek.FRIDAY) {
      return FRIDAY;
    }
    if (dayOfWeek == DayOfWeek.SATURDAY) {
      return SATURDAY;
    }
    throw new RuntimeException("Invalid day of week: " + dayOfWeek);
  }
}
