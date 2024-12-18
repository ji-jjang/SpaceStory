package com.juny.spacestory.domain.reservation.controller;

import com.juny.spacestory.domain.reservation.dto.ReqCreateReservation;
import com.juny.spacestory.domain.reservation.dto.ResCreateReservation;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.mapper.ReservationMapper;
import com.juny.spacestory.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/api")
@Controller
public class ReservationController {

  private final ReservationService reservationService;

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(
      summary = "시간제 또는 패키지 예약 생성 API",
      description =
          "사용자는 가격 타입(time, package), 예약일 및 예약 인원을 가지고 예약을 생성<br>시간제 예약인 경우 1) 전체 슬롯 가격 X 인원 수 2) 슬롯 가격 + (추가 인원 요금 X 추가 인원) <br>패키지 가격은 패키지 슬롯 가격 + (추가 인원 요금 X 추가 인원)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "예약 생성 성공"),
      })
  @PostMapping("/v1/detailed-spaces/{detailedSpaceId}/reservations")
  public ResponseEntity<ResCreateReservation> createReservation(
      @RequestBody ReqCreateReservation req, @PathVariable Long detailedSpaceId) {

    Reservation reservation = reservationService.createReservation(req, detailedSpaceId, -1L);

    return new ResponseEntity<>(
        ReservationMapper.toResReservation(reservation), HttpStatus.CREATED);
  }
}
