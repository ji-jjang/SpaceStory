package com.juny.spacestory.domain.space.common.entity;

import com.juny.spacestory.domain.user.common.entity.User;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class Space {

  private Long id;
  private String name;
  private String description;
  private String notice;
  private String address;

  private LocalTime openingTime;
  private LocalTime closingTime;
  private Integer floor;
  private Boolean hasElevator;
  private Boolean hasParking;

  private Boolean isAdvertised;
  private String verifiedStatus;

  private User user;

  private List<DetailedSpace> detailedSpaces;
  private List<SubCategory> subCategories;
  private List<HashTag> hashTags;
  private MainCategory mainCategory;
  private List<SpaceImage> spaceImages;
}
