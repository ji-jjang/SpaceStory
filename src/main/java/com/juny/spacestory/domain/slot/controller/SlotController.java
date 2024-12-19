package com.juny.spacestory.domain.slot.controller;

import com.juny.spacestory.domain.slot.dto.PriceInfo;
import com.juny.spacestory.domain.slot.dto.ResPrice;
import com.juny.spacestory.domain.slot.mapper.SlotMapper;
import com.juny.spacestory.domain.slot.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SlotController {

  private final SlotService slotService;

  @Tag(name = "슬롯 API", description = "슬롯 생성, 조회, 수정, 삭제 API")
  @Operation(
      summary = "시간제 또는 패키지 슬롯 생성 API",
      description = "기본적으로 생성달 제외 3개월 슬롯 생성<br>조회된 가격 정보 토대로 슬롯 생성 (시간제, 패키지 슬롯 둘 다 생성 가능)\n")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "슬롯 생성 성공"),
      })
  @PostMapping("/v1/detailed-spaces/{detailedSpaceId}/slots")
  public ResponseEntity<ResPrice> createSlots(
      @PathVariable Long detailedSpaceId, @RequestParam(defaultValue = "3") int month) {

    PriceInfo priceInfo = slotService.createSlots(detailedSpaceId, month);

    ResPrice price =
        new ResPrice(
            SlotMapper.toResTimePrice(priceInfo.timePrices()),
            SlotMapper.toResPackagePrice(priceInfo.packagePrices()));

    return new ResponseEntity<>(price, HttpStatus.OK);
  }

  @Tag(name = "슬롯 API", description = "슬롯 생성, 조회, 수정, 삭제 API")
  @Operation(summary = "시간제 또는 패키지 슬롯 조회 API", description = "기본적으로 조회 기준달 제외 3개월 슬롯 조회")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "슬롯 조회 성공"),
      })
  @GetMapping("/v1/detailed-spaces/{detailedSpaceId}/slots")
  public ResponseEntity<ResPrice> getSlots(
      @PathVariable Long detailedSpaceId,
      @RequestParam String type,
      @RequestParam(defaultValue = "3") int month) {

    ResPrice slots = slotService.getSlots(detailedSpaceId, type, month);

    return new ResponseEntity<>(slots, HttpStatus.OK);
  }

  @Tag(name = "슬롯 API", description = "슬롯 생성, 조회, 수정, 삭제 API")
  @Operation(
      summary = "가격 정보(시간제, 패키지) 기반으로 슬롯 수정 API",
      description = "기존 슬롯을 삭제하고, 기본값으로 기준달 제외 3개월 슬롯 생성, 동기화 옵션을 통해 기존 예약 정보와 동기화 가능")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "슬롯 수정 성공"),
      })
  @PutMapping("/v1/detailed-spaces/{detailedSpaceId}/slots")
  public ResponseEntity<ResPrice> updateSlots(
      @PathVariable Long detailedSpaceId,
      @RequestParam(defaultValue = "3") int month,
      @RequestParam Boolean isSync) {

    PriceInfo priceInfo = slotService.updateSlots(detailedSpaceId, month, isSync);

    return new ResponseEntity<>(
        new ResPrice(
            SlotMapper.toTimePricesUpdateIds(
                priceInfo.timePrices(), priceInfo.updatedTimeSlotIds()),
            SlotMapper.toPackagePricesUpdateIds(
                priceInfo.packagePrices(), priceInfo.updatedPackageSlotIds())),
        HttpStatus.OK);
  }
}
