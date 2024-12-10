package com.juny.spacestory.domain.price.entity;

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
  private Integer priceType;
  private Integer dayType;
  private LocalTime startTime;
  private LocalTime endTime;
  private String name;
  private Integer price;

  public static class BasePriceInformationBuilder {

    public BasePriceInformationBuilder priceType(PriceType priceType) {
      this.priceType = priceType.getNum();
      return this;
    }

    public BasePriceInformationBuilder dayType(DayType dayType) {
      this.dayType = dayType.getNum();
      return this;
    }
  }
}
