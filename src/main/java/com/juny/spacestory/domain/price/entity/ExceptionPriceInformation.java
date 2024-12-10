package com.juny.spacestory.domain.price.entity;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ExceptionPriceInformation {

  private Long id;
  private String name;
  private LocalDate startDate;
  private LocalDate endDate;

  private List<ExceptionPriceDetail> exceptionPriceDetails;
}
