package com.juny.spacestory.domain.reservation.user.controller;

import com.juny.spacestory.domain.reservation.common.dto.ReqReservationCreate;
import com.juny.spacestory.domain.reservation.common.dto.ReqReservationList;
import com.juny.spacestory.domain.reservation.common.dto.ResReservation;
import com.juny.spacestory.domain.reservation.common.dto.ResReservationList;
import com.juny.spacestory.domain.reservation.common.dto.SearchCondition;
import com.juny.spacestory.domain.reservation.common.entity.Reservation;
import com.juny.spacestory.domain.reservation.common.mapper.ReservationMapper;
import com.juny.spacestory.domain.reservation.common.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
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
  public ResponseEntity<ResReservation> createReservation(
      @RequestBody ReqReservationCreate req, @PathVariable Long detailedSpaceId) {

    Reservation reservation = reservationService.createReservation(req, detailedSpaceId, -1L);

    return new ResponseEntity<>(
        ReservationMapper.toResReservation(reservation), HttpStatus.CREATED);
  }

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(
      summary = "사용자 예약 목록 전체 조회 API",
      description =
          "정렬 조건: 생성일(created_at:desc or asc) 오름차순, 내림차순 및 이용일자(startDateTime:desc or asc) 오름차순 내림차순")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "예약 조회 성공"),
      })
  @GetMapping("/v1/user/reservations")
  public ResponseEntity<ResReservationList> getReservationsByUser(
      @ModelAttribute ReqReservationList req) {

    SearchCondition searchCondition =
        reservationService.getReservationListSearchCondition(req, -1L, false);

    long totalReservationCount = reservationService.getTotalReservationCountByUser(searchCondition);

    List<Reservation> reservationList =
        reservationService.getReservationListByUser(searchCondition);

    ResReservationList resReservationListForUser =
        ReservationMapper.toResReservationList(
            reservationList, searchCondition, totalReservationCount);

    return new ResponseEntity<>(resReservationListForUser, HttpStatus.OK);
  }

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(
      summary = "호스트 예약 목록 전체 조회 API",
      description =
          "정렬 조건: 생성일(created_at:desc or asc) 오름차순, 내림차순 및 이용일자(startDateTime:desc or asc) 오름차순 내림차순<br>호스트가 가진 모든 상세 공간 아이디와 매칭되는 예약 조회")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "예약 전체 조회 성공"),
      })
  @GetMapping("/v1/host/reservations")
  public ResponseEntity<ResReservationList> getReservationsByHost(
      @ModelAttribute ReqReservationList req) {

    SearchCondition searchCondition =
        reservationService.getReservationListSearchCondition(req, -1L, false);

    long totalReservationCount = reservationService.getTotalReservationCountByHost(searchCondition);

    List<Reservation> reservationList =
        reservationService.getReservationListByHost(searchCondition);

    ResReservationList resReservationListForHost =
        ReservationMapper.toResReservationList(
            reservationList, searchCondition, totalReservationCount);

    return new ResponseEntity<>(resReservationListForHost, HttpStatus.OK);
  }

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(summary = "사용자, 호스트 예약 목록 단건 조회 API", description = "사용자, 호스트 접근 권한 검사 로직 존재")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "사용자, 호스트 예약 단건 조회 성공"),
      })
  @GetMapping("/v1/detailed-spaces/{detailedSpaceId}/reservations/{reservationId}")
  public ResponseEntity<ResReservation> getReservationByUser(
      @PathVariable Long detailedSpaceId, @PathVariable Long reservationId) {

    Reservation reservation =
        reservationService.getReservationByReservationIdByUser(detailedSpaceId, reservationId, -1L);

    ResReservation resReservation = ReservationMapper.toResReservation(reservation);

    return new ResponseEntity<>(resReservation, HttpStatus.OK);
  }

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(
      summary = "사용자 예약 변경 API",
      description = "기존 승인된 예약을 새로운 예약으로 변경<br>기존 예약 상태는 취소 대기, 새로운 예약 상태는 승인 대기")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "사용자 예약 변경 요청 성공"),
      })
  @PatchMapping("/v1/detailed-spaces/{detailedSpaceId}/reservations/{reservationId}")
  public ResponseEntity<ResReservation> updateReservation(
      @RequestBody ReqReservationCreate req,
      @PathVariable Long detailedSpaceId,
      @PathVariable Long reservationId) {

    Reservation updatedReservation =
        reservationService.updateReservationByUser(req, detailedSpaceId, reservationId);

    ResReservation resReservation = ReservationMapper.toResReservation(updatedReservation);

    return new ResponseEntity<>(resReservation, HttpStatus.OK);
  }

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(summary = "사용자 예약 취소 API", description = "예약 승인 상태를 취소 대기 상태로 변경")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "사용자 예약 취소 요청 성공"),
      })
  @PatchMapping("/v1/reservations/{reservationId}/cancel")
  public ResponseEntity<ResReservation> cancelReservation(@PathVariable Long reservationId) {

    Reservation reservation = reservationService.cancelReservationByUser(reservationId);

    ResReservation resReservation = ReservationMapper.toResReservation(reservation);

    return new ResponseEntity<>(resReservation, HttpStatus.OK);
  }

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(summary = "호스트 예약 변경 요청 승인 API", description = "새로운 예약 승인 대기에서 승인, 기존 예약 취소 대기에서 취소")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "사용자의 예약 변경 요청, 호스트 승인 성공"),
      })
  @PatchMapping("/v1/reservations/{reservationId}/approve")
  public ResponseEntity<ResReservation> approveUpdateReservation(@PathVariable Long reservationId) {

    Reservation reservation = reservationService.approveUpdateReservationByHost(reservationId);

    ResReservation resReservation = ReservationMapper.toResReservation(reservation);

    return new ResponseEntity<>(resReservation, HttpStatus.OK);
  }

  @Tag(name = "예약 API", description = "예약 생성, 조회, 수정, 삭제 API")
  @Operation(summary = "호스트 예약 변경 요청 거절 API", description = "새로운 예약 승인 대기에서 거절, 기존 예약 취소 대기에서 승인")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "사용자의 예약 변경 요청, 호스트 거절 성공"),
      })
  @PatchMapping("/v1/reservations/{reservationId}/reject")
  public ResponseEntity<ResReservation> rejectUpdateReservation(@PathVariable Long reservationId) {

    Reservation reservation = reservationService.rejectUpdateReservationByHost(reservationId);

    ResReservation resReservation = ReservationMapper.toResReservation(reservation);

    return new ResponseEntity<>(resReservation, HttpStatus.OK);
  }
}
