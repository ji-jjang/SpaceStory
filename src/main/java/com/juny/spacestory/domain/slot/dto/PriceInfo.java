package com.juny.spacestory.domain.slot.dto;

import com.juny.spacestory.domain.slot.entity.PackagePrice;
import com.juny.spacestory.domain.slot.entity.TimePrice;
import java.util.List;

public record PriceInfo(
    List<TimePrice> timePrices,
    List<PackagePrice> packagePrices,
    List<Long> updatedTimeSlotIds,
    List<Long> updatedPackageSlotIds) {}
