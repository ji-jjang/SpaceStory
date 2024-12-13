package com.juny.spacestory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.juny.spacestory.domain.advertise.entity.AdvertiseCoupon;
import com.juny.spacestory.domain.advertise.repository.AdvertiseCouponRepository;
import com.juny.spacestory.domain.advertise.service.AdvertiseCouponService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

  @InjectMocks private AdvertiseCouponService couponService;

  @Mock private AdvertiseCouponRepository advertiseCouponRepository;

  @Test
  @DisplayName("3개월 광고 쿠폰을 생성한다")
  void createAdvertiseCoupons() {

    // given
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusMonths(3);
    int price = 40000;
    AdvertiseCoupon expected =
        AdvertiseCoupon.builder().startDate(startDate).endDate(endDate).price(price).build();

    when(advertiseCouponRepository.save(any(AdvertiseCoupon.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    AdvertiseCoupon coupon = couponService.createAdvertiseCoupon(3);

    // then
    assertThat(coupon).isNotNull();
    assertThat(coupon.getStartDate()).isEqualTo(expected.getStartDate());
    assertThat(coupon.getEndDate()).isEqualTo(expected.getEndDate());
    assertThat(coupon.getPrice()).isEqualTo(expected.getPrice());
  }

  @Test
  @DisplayName("만료된 쿠폰을 광고에 부착한다")
  void attachCouponToSpace() {

    // given
    AdvertiseCoupon coupon =
        AdvertiseCoupon.builder()
            .id(-1L)
            .startDate(LocalDate.now().minusMonths(4))
            .endDate(LocalDate.now().minusMonths(1))
            .price(-1)
            .space(null)
            .build();

    when(advertiseCouponRepository.findById(any(Long.class))).thenReturn(Optional.of(coupon));

    // when & then
    assertThatThrownBy(() -> couponService.updateAdvertiseCoupon(-1L, -1L, true))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("쿠폰 유효기간이 만료되었습니다.");
  }
}
