package com.juny.spacestory.domain.advertise.entity;

import com.juny.spacestory.domain.space.entity.Space;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class AdvertiseCoupon {

  private Long id;
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer price;
  private Space space;
}
