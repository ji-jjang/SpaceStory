package com.juny.spacestory.domain.price.controller;

import com.juny.spacestory.domain.price.dto.ReqCreateSlot;
import com.juny.spacestory.domain.price.dto.ResPrice;
import com.juny.spacestory.domain.price.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SlotController {

  private final SlotService slotService;

  @Tag(name = "슬롯 API", description = "슬롯 생성, 조회, 수정, 삭제 API")
  @Operation(summary = "시간제 또는 패키지 슬롯 생성 API", description = "기본적으로 생성달 제외 3개월 슬롯 생성")
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "슬롯 생성 성공"),
    })

  @PostMapping("/v1/admin/detailed-spaces/{detailedSpaceId}/slots")
  public ResponseEntity<ResPrice> createSlots(
    @PathVariable Long detailedSpaceId,
    @RequestParam(defaultValue = "3") int month,
    @Validated @RequestBody ReqCreateSlot req) {

    ResPrice slots = slotService.createSlots(detailedSpaceId, month, req);

    return new ResponseEntity<>(slots, HttpStatus.OK);
  }

  @Tag(name = "슬롯 API", description = "슬롯 생성, 조회, 수정, 삭제 API")
  @Operation(summary = "시간제 또는 패키지 슬롯 조회 API", description = "기본적으로 조회 기준달 제외 3개월 슬롯 조회")
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "슬롯 조회 성공"),
    })

  @GetMapping("/v1/detailed_spaces/{detailedSpaceId}/slots")
  public ResponseEntity<ResPrice> getSlots(@PathVariable Long detailedSpaceId,
    @RequestParam String type, @RequestParam(defaultValue = "3") int month) {

    ResPrice slots = slotService.getSlots(detailedSpaceId, type, month);

    return new ResponseEntity<>(slots, HttpStatus.OK);
  }
}
