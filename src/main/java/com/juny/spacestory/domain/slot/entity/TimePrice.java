package com.juny.spacestory.domain.slot.entity;

import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class TimePrice {

  private Long id;
  private YearMonth yearAndMonth;

  private List<TimeDayPrice> timeDayPrices;
}
