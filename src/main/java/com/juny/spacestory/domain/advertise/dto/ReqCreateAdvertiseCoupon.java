package com.juny.spacestory.domain.advertise.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReqCreateAdvertiseCoupon(
  @NotNull(message = "month not null")
  @NotEmpty(message = "month not empty")
  int month
) {

}
