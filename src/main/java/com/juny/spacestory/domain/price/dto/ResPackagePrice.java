package com.juny.spacestory.domain.price.dto;

import java.util.List;

public record ResPackagePrice(String yearAndMonth, List<ResDayPackagePrice> dayPackagePrices) {}
