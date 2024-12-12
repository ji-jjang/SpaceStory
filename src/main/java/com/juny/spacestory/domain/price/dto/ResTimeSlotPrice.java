package com.juny.spacestory.domain.price.dto;

public record ResTimeSlotPrice(
  String startTime,
  Integer price,
  Boolean isReserved
) {

}
