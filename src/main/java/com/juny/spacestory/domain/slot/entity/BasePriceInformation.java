package com.juny.spacestory.domain.slot.entity;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BasePriceInformation implements PriceInfo {

  private Long id;
  private String priceType;
  private Integer dayType;
  private LocalTime startTime;
  private LocalTime endTime;
  private String name;
  private Integer price;

  public static class BasePriceInformationBuilder {

    public BasePriceInformationBuilder dayType(DayType dayType) {
      this.dayType = dayType.getNum();
      return this;
    }
  }
}
