package com.juny.spacestory.domain.advertise.user.controller;

import com.juny.spacestory.domain.advertise.common.dto.ReqCreateAdvertiseCoupon;
import com.juny.spacestory.domain.advertise.common.dto.ReqUpdateSpaceAdvertising;
import com.juny.spacestory.domain.advertise.common.dto.ResCreateAdvertiseCoupon;
import com.juny.spacestory.domain.advertise.common.dto.ResUpdateAdvertiseCoupon;
import com.juny.spacestory.domain.advertise.common.entity.AdvertiseCoupon;
import com.juny.spacestory.domain.advertise.common.mapper.CouponMapper;
import com.juny.spacestory.domain.advertise.common.service.AdvertiseCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdvertiseCouponController {

  private final AdvertiseCouponService advertiseCouponService;

  @Tag(name = "광고 쿠폰 API", description = "광고 쿠폰 발행, 광고 쿠폰 공간에 부착 또는 해제")
  @Operation(summary = "광고 쿠폰 1, 3, 6개월 단위로 발행하는 API")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "광고 쿠폰 발행 성공"),
      })
  @PostMapping("/v1/advertise-coupons")
  public ResponseEntity<ResCreateAdvertiseCoupon> createAdvertiseCoupon(
      @Validated @RequestBody ReqCreateAdvertiseCoupon req) {

    AdvertiseCoupon savedCoupon = advertiseCouponService.createAdvertiseCoupon(req.month());

    ResCreateAdvertiseCoupon resCoupon = CouponMapper.toResCreateCoupon(savedCoupon);

    return new ResponseEntity<>(resCoupon, HttpStatus.OK);
  }

  @Tag(name = "광고 쿠폰 API", description = "광고 쿠폰 발행, 광고 쿠폰 공간에 부착 또는 해제")
  @Operation(
      summary = "광고 쿠폰 공간에 부착 또는 해제 API",
      description =
          "쿠폰이 부착된 공간은 is_advertised 컬럼이 true, 검색에 노출<br>유효기간이 만료된 공간은 is_advertise 컬럼 false 변경. (월초에 스케줄러 또는 배치 고려)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "공간에 쿠폰 부착 또는 해제 성공"),
      })
  @PatchMapping("/v1/spaces/{spaceId}/coupons/{couponId}")
  public ResponseEntity<ResUpdateAdvertiseCoupon> updateSpaceAdvertising(
      @PathVariable Long spaceId,
      @PathVariable Long couponId,
      @Validated @RequestBody ReqUpdateSpaceAdvertising req) {

    advertiseCouponService.updateAdvertiseCoupon(spaceId, couponId, req.isAdvertised());

    return new ResponseEntity<>(new ResUpdateAdvertiseCoupon(req.isAdvertised()), HttpStatus.OK);
  }
}
