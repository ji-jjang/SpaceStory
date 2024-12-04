package com.juny.spacestory.domain.reservation.entity;

import lombok.Getter;

@Getter
public enum ReservationType {
  TIME(0),
  PACKAGE(1);

  private final int num;

  ReservationType(int num) {
    this.num = num;
  }
}
