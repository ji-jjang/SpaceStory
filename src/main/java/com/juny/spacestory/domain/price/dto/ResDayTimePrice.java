package com.juny.spacestory.domain.price.dto;

import java.util.List;

public record ResDayTimePrice(
  Integer day,
  Boolean isAllReserved,
  List<ResTimeSlotPrice> timeSlotPrices
) {

}
