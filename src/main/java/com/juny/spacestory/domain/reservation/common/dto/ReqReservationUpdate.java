package com.juny.spacestory.domain.reservation.common.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ReqReservationUpdate(Long id, String status, LocalDateTime deletedAt) {}
