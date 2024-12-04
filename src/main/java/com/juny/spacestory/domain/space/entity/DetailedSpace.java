package com.juny.spacestory.domain.space.entity;

import com.juny.spacestory.domain.reservation.entity.Price;
import com.juny.spacestory.domain.reservation.entity.PriceInfo;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.entity.ReservationInfo;
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
  private Integer size;
  private String representImagePath;

  private Space space;
  private ReservationInfo reservationInfo;
  private List<DetailedSpaceImage> detailedSpaceImages;
  private List<PriceInfo> priceInfos;
  private List<Price> prices;
}
