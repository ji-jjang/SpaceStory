package com.juny.spacestory.domain.slot.dto;

public record ResPackageSlotPrice(
    Long id, String name, String startTime, String endTime, Integer price, Boolean isReserved) {}
