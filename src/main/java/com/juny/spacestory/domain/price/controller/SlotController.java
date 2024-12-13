package com.juny.spacestory.domain.price.controller;

import com.juny.spacestory.domain.price.dto.ReqCreateSlot;
import com.juny.spacestory.domain.price.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
      @ApiResponse(responseCode = "204", description = "슬롯 생성 성공"),
    })

  @PostMapping("/v1/admin/detailed-spaces/{detailedSpaceId}/slots")
  public ResponseEntity<Void> createSlots(
    @PathVariable Long detailedSpaceId,
    @RequestParam(defaultValue = "3") int creationMonth,
    @Validated @RequestBody ReqCreateSlot req) {

    slotService.createSlots(detailedSpaceId, creationMonth, req);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
