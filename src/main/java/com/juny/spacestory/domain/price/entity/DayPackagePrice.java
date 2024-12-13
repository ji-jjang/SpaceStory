package com.juny.spacestory.domain.price.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DayPackagePrice {

  private Long id;
  private Integer day;

  List<PackageSlotPrice> packageSlotPrices;
}
