package com.juny.spacestory.domain.reservation.dto;

import java.util.List;

public record SearchCondition(
    String startDate,
    String endDate,
    Integer page,
    Integer pageSize,
    Integer offset,
    String sortField,
    String sortDirection,
    Long userId,
    List<Long> detailedSpaceIds) {}
