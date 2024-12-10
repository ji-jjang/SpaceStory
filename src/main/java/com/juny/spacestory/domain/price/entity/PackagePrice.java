package com.juny.spacestory.domain.price.entity;

import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PackagePrice {

  private Long id;
  private YearMonth yearAndMonth;

  private List<DayPackagePrice> dayPackagePrices;
}
