package com.juny.spacestory.domain.price.dto;

import com.juny.spacestory.domain.price.entity.PackagePrice;
import com.juny.spacestory.domain.price.entity.TimePrice;
import java.util.List;

public record PriceInfo(
    List<TimePrice> timePrices,
    List<PackagePrice> packagePrices,
    List<Long> updatedTimeSlotIds,
    List<Long> updatedPackageSlotIds) {}
