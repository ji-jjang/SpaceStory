package com.juny.spacestory.domain.price.entity;

import lombok.Getter;

@Getter
public enum PriceType {
  TIME(0),
  PACKAGE(1);

  private final int num;

  PriceType(int num) {
    this.num = num;
  }
}
