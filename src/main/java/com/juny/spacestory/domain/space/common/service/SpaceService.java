package com.juny.spacestory.domain.space.common.service;

import com.juny.spacestory.domain.space.common.dto.ReqCreateSpace;
import com.juny.spacestory.domain.space.common.entity.HashTag;
import com.juny.spacestory.domain.space.common.entity.MainCategory;
import com.juny.spacestory.domain.space.common.entity.Space;
import com.juny.spacestory.domain.space.common.entity.SpaceHashtag;
import com.juny.spacestory.domain.space.common.entity.SpaceImage;
import com.juny.spacestory.domain.space.common.entity.SpaceSubCategory;
import com.juny.spacestory.domain.space.common.entity.SubCategory;
import com.juny.spacestory.domain.space.common.repository.HashTagRepository;
import com.juny.spacestory.domain.space.common.repository.MainCategoryRepository;
import com.juny.spacestory.domain.space.common.repository.SpaceHashTagRepository;
import com.juny.spacestory.domain.space.common.repository.SpaceImageRepository;
import com.juny.spacestory.domain.space.common.repository.SpaceRepository;
import com.juny.spacestory.domain.space.common.repository.SpaceSubCategoryRepository;
import com.juny.spacestory.domain.space.common.repository.SubCategoryRepository;
import com.juny.spacestory.domain.user.common.entity.User;
import com.juny.spacestory.domain.user.common.repository.UserRepository;
import com.juny.spacestory.global.constant.Constants;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpaceService {

  private final SpaceRepository spaceRepository;

  private final UserRepository userRepository;

  private final MainCategoryRepository mainCategoryRepository;

  private final SubCategoryRepository subCategoryRepository;

  private final HashTagRepository hashTagRepository;

  private final SpaceImageRepository spaceImageRepository;

  private final SpaceSubCategoryRepository spaceSubCategoryRepository;

  private final SpaceHashTagRepository spaceHashTagRepository;

  private final Clock clock;

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

  public Space getSpaceDetailById(Long spaceId) {

    return spaceRepository
        .findSpaceDetailById(spaceId)
        .orElseThrow(() -> new RuntimeException(String.format("invalid space id: %d", spaceId)));
  }

  @Transactional
  public Space createSpace(ReqCreateSpace req, Long userId) {

    User user = getUser(userId);

    LocalTime openingTime = LocalTime.parse(req.openingTime());
    LocalTime closingTime = LocalTime.parse(req.closingTime());

    Space space =
        Space.builder()
            .name(req.name())
            .description(req.description())
            .notice(req.notice())
            .address(req.address())
            .openingTime(openingTime)
            .closingTime(closingTime)
            .hasElevator(req.hasElevator())
            .hasParking(req.hasParking())
            .isAdvertised(false)
            .verifiedStatus(Constants.SPACE_STATUS_APPROVE_PENDING)
            .user(user)
            .build();

    spaceRepository.save(space);

    MainCategory mainCategory = getMainCategory(req);

    List<SubCategory> subCategories = getSubCategories(req, mainCategory, space.getId());

    List<HashTag> hashTags = getHashTags(req, space.getId());

    List<SpaceImage> spaceImages = getSpaceImages(req);

    return space.toBuilder()
        .subCategories(subCategories)
        .hashTags(hashTags)
        .spaceImages(spaceImages)
        .mainCategory(mainCategory)
        .build();
  }

  private User getUser(Long userId) {

    return userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException(String.format("invalid user id: %d", userId)));
  }

  private List<SpaceImage> getSpaceImages(ReqCreateSpace req) {
    List<SpaceImage> spaceImages = new ArrayList<>();

    if (req.image().getSize() > 0) {
      String logicalName = req.image().getOriginalFilename();
      String storedName = UUID.randomUUID().toString().replaceAll("-", "");
      String extension = "";
      if (logicalName.contains(".")) {
        extension = logicalName.substring(logicalName.lastIndexOf("."));
      }
      long size = req.image().getSize();
      SpaceImage image =
          SpaceImage.builder()
              .logicalName(logicalName)
              .storedName(storedName)
              .extension(extension)
              .size(size)
              .isRepresent(true)
              .createdAt(LocalDateTime.now(clock))
              .build();

      spaceImages.add(image);
    }

    for (var imageFile : req.images()) {

      if (imageFile.getSize() < 1) continue;

      String logicalName = imageFile.getOriginalFilename();
      String storedName = UUID.randomUUID().toString().replaceAll("-", "");
      String extension = "";
      if (logicalName.contains(".")) {
        extension = logicalName.substring(logicalName.lastIndexOf("."));
      }
      long size = imageFile.getSize();
      SpaceImage image =
          SpaceImage.builder()
              .logicalName(logicalName)
              .storedName(storedName)
              .extension(extension)
              .size(size)
              .isRepresent(false)
              .createdAt(LocalDateTime.now(clock))
              .build();

      spaceImages.add(image);
    }

    spaceImageRepository.saveAll(spaceImages);
    return spaceImages;
  }

  private List<HashTag> getHashTags(ReqCreateSpace req, Long spaceId) {

    List<HashTag> hashTags = new ArrayList<>();
    List<SpaceHashtag> spaceHashtags = new ArrayList<>();
    for (var tagName : req.hashtags()) {

      HashTag hashtag =
          hashTagRepository
              .findByName(tagName)
              .orElseGet(
                  () -> {
                    HashTag newTag = HashTag.builder().name(tagName).build();
                    hashTagRepository.save(newTag);
                    return newTag;
                  });

      hashTags.add(hashtag);
      SpaceHashtag spaceHashtag =
          SpaceHashtag.builder().spaceId(spaceId).hashtagId(hashtag.getId()).build();
      spaceHashtags.add(spaceHashtag);
    }

    spaceHashTagRepository.saveAll(spaceHashtags);

    return hashTags;
  }

  private List<SubCategory> getSubCategories(
      ReqCreateSpace req, MainCategory mainCategory, Long spaceId) {

    List<SubCategory> subCategories = new ArrayList<>();

    List<SpaceSubCategory> spaceSubCategories = new ArrayList<>();
    for (var subCategoryId : req.subCategoryIds()) {
      SubCategory subCategory =
          subCategoryRepository
              .findById(subCategoryId)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          String.format("invalid sub category id: %d", subCategoryId)));

      if (!subCategory.getMainCategory().getName().equals(mainCategory.getName())) {
        throw new RuntimeException(
            String.format(
                "subCategory:%s not contain mainCategory:%s",
                subCategory.getName(), mainCategory.getName()));
      }
      subCategories.add(subCategory);

      SpaceSubCategory spaceSubCategory =
          SpaceSubCategory.builder().spaceId(spaceId).subCategoryId(subCategoryId).build();

      spaceSubCategories.add(spaceSubCategory);
    }

    return subCategories;
  }

  private MainCategory getMainCategory(ReqCreateSpace req) {

    return mainCategoryRepository
        .findById(req.mainCategoryId())
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format("invalid main category id: %d", req.mainCategoryId())));
  }
}
