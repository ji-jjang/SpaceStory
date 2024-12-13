package com.juny.spacestory.domain.price.dto;

import java.util.List;

public record ResTimePrice(String yearAndMonth, List<ResDayTimePrice> dayTimePrices) {}
