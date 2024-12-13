package com.juny.spacestory.domain.price.dto;

public record ResPackageSlotPrice(
    Long id, String name, String startTime, String endTime, Integer price, Boolean isReserved) {}
