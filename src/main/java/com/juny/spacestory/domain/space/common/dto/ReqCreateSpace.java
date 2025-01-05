package com.juny.spacestory.domain.space.common.dto;

import java.util.List;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record ReqCreateSpace(
    String name,
    Long mainCategoryId,
    List<Long> subCategoryIds,
    String description,
    List<String> hashtags,
    String notice,
    MultipartFile image,
    List<MultipartFile> images,
    String address,
    String openingTime,
    String closingTime,
    Integer floor,
    Boolean hasElevator,
    Boolean hasParking) {}

