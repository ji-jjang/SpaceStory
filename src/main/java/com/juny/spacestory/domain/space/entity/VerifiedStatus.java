package com.juny.spacestory.domain.space.entity;

import lombok.Getter;

@Getter
public enum VerifiedStatus {

  PENDING(0),
  APPROVED(1),
  REJECTED(2);

  private final int num;

  VerifiedStatus(int num) {
    this.num = num;
  }
}
