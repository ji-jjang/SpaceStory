package com.juny.spacestory.domain.slot.dto;

import java.util.List;

public record ResPackagePrice(String yearAndMonth, List<ResDayPackagePrice> dayPackagePrices) {}
