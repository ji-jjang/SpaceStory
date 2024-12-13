package com.juny.spacestory.domain.price.dto;

public record ResTimeSlotPrice(
  Long id,
  String startTime,
  Integer price,
  Boolean isReserved
) {

}
