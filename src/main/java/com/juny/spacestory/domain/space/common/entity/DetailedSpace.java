package com.juny.spacestory.domain.space.common.entity;

import com.juny.spacestory.domain.reservation.common.entity.ReservationPriceInfo;
import com.juny.spacestory.domain.slot.entity.BasePriceInformation;
import com.juny.spacestory.domain.slot.entity.ExceptionPriceInformation;
import com.juny.spacestory.domain.slot.entity.PackagePrice;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DetailedSpace {

  private Long id;
  private String name;
  private String description;
  private Integer size;
  private String representImagePath;
  private Integer minimalCapacity;
  private Integer maximalCapacity;

  private Space space;
  private ReservationPriceInfo reservationInfo;
  private List<SubCategory> subCategories;
  private List<DetailedSpaceImage> detailedSpaceImages;
  private List<BasePriceInformation> basePriceInformation;
  private List<ExceptionPriceInformation> exceptionPriceInformation;
  private List<PackagePrice> prices;
}
