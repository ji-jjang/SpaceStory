package com.juny.spacestory.domain.reservation.entity;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * yearMonthDay -> Day: 고정된 접미사(01) 사용
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Price {

  private Long id;
  private LocalDate yearMonthDay;

  private List<DayTimePrice> dayTimePrices;
  private List<DayPackagePrice> dayPackagePrices;
}
