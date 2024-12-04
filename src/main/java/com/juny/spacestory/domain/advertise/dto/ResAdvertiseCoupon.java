package com.juny.spacestory.domain.advertise.dto;

import java.time.LocalDate;

public record ResAdvertiseCoupon(
  Long id,
  LocalDate startDate,
  LocalDate endDate,
  Integer price,
  Long spaceId
) {

}
