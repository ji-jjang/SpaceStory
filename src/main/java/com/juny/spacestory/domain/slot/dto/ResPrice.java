package com.juny.spacestory.domain.slot.dto;

import java.util.List;

public record ResPrice(List<ResTimePrice> timePrices, List<ResPackagePrice> packagePrices) {}
