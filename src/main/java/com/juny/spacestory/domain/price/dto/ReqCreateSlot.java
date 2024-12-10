package com.juny.spacestory.domain.price.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReqCreateSlot(
  @NotNull(message = "createTimeSlot not null")
  @NotEmpty(message = "createTimeSlot not empty")
  Boolean createTimeSlot,

  @NotNull(message = "createPackageSlot not null")
  @NotEmpty(message = "createPackageSlot not empty")
  Boolean createPackageSlot
) {

}
