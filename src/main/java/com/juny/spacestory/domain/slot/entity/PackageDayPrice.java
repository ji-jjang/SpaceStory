package com.juny.spacestory.domain.slot.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class PackageDayPrice {

  private Long id;
  private Integer day;

  private List<PackageSlotPrice> packageSlotPrices;
}
