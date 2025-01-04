package com.juny.spacestory.domain.advertise.common.dto;

import java.time.LocalDate;

public record ResCreateAdvertiseCoupon(
    Long id, LocalDate startDate, LocalDate endDate, Integer price, Long spaceId) {}
