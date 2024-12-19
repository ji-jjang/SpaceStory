package com.juny.spacestory.domain.reservation.controller;

import com.juny.spacestory.domain.reservation.dto.ReqReservationCreate;
import com.juny.spacestory.domain.reservation.dto.ReqReservationList;
import com.juny.spacestory.domain.reservation.dto.ResReservation;
import com.juny.spacestory.domain.reservation.dto.ResReservationList;
import com.juny.spacestory.domain.reservation.dto.SearchCondition;
import com.juny.spacestory.domain.reservation.entity.Reservation;
import com.juny.spacestory.domain.reservation.mapper.ReservationMapper;
import com.juny.spacestory.domain.reservation.service.ReservationService;
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
  @Operation(
      summary = "관리자 예약 목록 전체 조회 API",
      description =
          "정렬 조건: 생성일(created_at:desc or asc) 오름차순, 내림차순 및 이용일자(startDateTime:desc or asc) 오름차순 내림차순<br>관리자가 모든 예약 조회, 호스트 및 사용자와 달리 키워드 검색 기능")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "예약 전체 조회 성공"),
      })
  @GetMapping("/admin/v1/reservations")
  public ResponseEntity<ResReservationList> getReservationByAdmin(
      @ModelAttribute ReqReservationList req) {

    SearchCondition searchCondition =
        reservationService.getReservationListSearchCondition(req, -1L, false);

    long totalReservationCount =
        reservationService.getTotalReservationCountByAdmin(searchCondition);

    List<Reservation> reservationList =
        reservationService.getReservationListByAdmin(searchCondition);

    ResReservationList resReservationListForAdmin =
        ReservationMapper.toResReservationList(
            reservationList, searchCondition, totalReservationCount);

    return new ResponseEntity<>(resReservationListForAdmin, HttpStatus.OK);
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
  @Operation(summary = "관리자 예약 목록 단건 조회 API")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "관리자, 예약 단건 조회 성공"),
      })
  @GetMapping("admin/v1/reservations/{reservationId}")
  public ResponseEntity<ResReservation> getReservationByHost(@PathVariable Long reservationId) {

    Reservation reservation = reservationService.getReservationByReservationIdByHost(reservationId);

    ResReservation resReservation = ReservationMapper.toResReservation(reservation);

    return new ResponseEntity<>(resReservation, HttpStatus.OK);
  }
}
