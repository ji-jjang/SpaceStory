package com.juny.spacestory.domain.price.dto;

public record ResPackageSlotPrice(
  String name,
  String startTime,
  String endTime,
  Integer price,
  Boolean isReserved
) {

}
