package com.juny.spacestory.domain.price.dto;

import java.util.List;

public record ResPrice(List<ResTimePrice> timePrices, List<ResPackagePrice> packagePrices) {}
