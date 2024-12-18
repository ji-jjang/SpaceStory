package com.juny.spacestory.domain.slot.dto;

import java.util.List;

public record ResTimePrice(String yearAndMonth, List<ResDayTimePrice> dayTimePrices) {}
