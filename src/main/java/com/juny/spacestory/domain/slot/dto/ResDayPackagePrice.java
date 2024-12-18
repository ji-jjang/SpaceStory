package com.juny.spacestory.domain.slot.dto;

import java.util.List;

public record ResDayPackagePrice(
    Integer day, Boolean isAllReserved, List<ResPackageSlotPrice> packageSlotPrices) {}
