package com.juny.spacestory.domain.point.user.controller;

import com.juny.spacestory.domain.point.common.entity.Point;
import com.juny.spacestory.domain.point.common.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PointController {

  private final PointService pointService;

  @Tag(name = "포인트 API", description = "포인트 충전, 조회 API")
  @Operation(summary = "사용자 포인트 기록 조회 API")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "사용자 포인트 기록 조회 성공"),
      })
  @GetMapping("/api/v1/user/points")
  public ResponseEntity<List<Point>> getUserPoints() {

    return new ResponseEntity<>(pointService.findAllByUserId(-1L), HttpStatus.OK);
  }

  @Tag(name = "포인트 API", description = "포인트 충전, 조회 API")
  @Operation(summary = "사용자 포인트 충전 API", description = "10_000, 50_000, 100_000 충전 가능")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "사용자 포인트 충전 성공"),
      })
  @PostMapping("/api/v1/user/points/charge")
  public ResponseEntity<Point> pointCharge(@RequestParam Integer amount) {

    return new ResponseEntity<>(pointService.chargePoint(amount, -1L), HttpStatus.CREATED);
  }
}
