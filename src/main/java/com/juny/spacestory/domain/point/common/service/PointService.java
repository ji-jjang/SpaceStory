package com.juny.spacestory.domain.point.common.service;

import com.juny.spacestory.domain.point.common.entity.Point;
import com.juny.spacestory.domain.point.common.repository.PointRepository;
import com.juny.spacestory.domain.user.common.entity.User;
import com.juny.spacestory.domain.user.common.repository.UserRepository;
import com.juny.spacestory.global.constant.Constants;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointService {

  public static HashSet<Integer> chargeAmountMap = new HashSet<>();

  static {
    chargeAmountMap.add(10_000);
    chargeAmountMap.add(50_000);
    chargeAmountMap.add(100_000);
  }

  private PointRepository pointRepository;

  private UserRepository userRepository;

  /**
   *
   *
   * <h1>유저 아이디로 모든 포인트 내역 조회 </h1>
   *
   * @param userId 유저 아이디
   * @return 유저 아이디로 조회된 포인트 기록
   */
  public List<Point> findAllByUserId(Long userId) {

    return pointRepository.findAllByUserId(userId);
  }

  /**
   *
   *
   * <h1>유저 포인트 충전 </h1>
   *
   * <br>
   * - 포인트 충전 후 사용자 currentPoint 현재 포인트 반영
   *
   * @param amount 충전하는 금액, 10000, 50000, 100000 지원
   * @param userId 사용자 아이디
   * @return 충전한 포인트
   */
  @Transactional
  public Point chargePoint(Integer amount, Long userId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException(String.format("invalid user id: %d", userId)));

    if (!chargeAmountMap.contains(amount)) {
      throw new RuntimeException("not support charge point");
    }

    Point point =
        Point.builder()
            .amount(amount)
            .reason(Constants.POINT_CHARGE_REASON)
            .createdAt(LocalDateTime.now())
            .user(User.builder().id(userId).build())
            .build();

    pointRepository.save(point);

    userRepository.updateCurrentPoint(user.getCurrentPoint() + amount);

    return point;
  }

  /**
   *
   *
   * <h1>포인트 추가, 감액시 포인트 생성 </h1>
   *
   * @param amount 추가(차감)할 포인트
   * @param user 유저
   * @param hostId 호스트 아이디(공간 예약의 경우)
   * @return 포인트
   */
  @Transactional
  public Point processPointPayment(Integer amount, User user, Long hostId, String reason) {

    Point point =
        Point.builder()
            .amount(amount)
            .reason(reason)
            .createdAt(LocalDateTime.now())
            .hostID(hostId)
            .user(user)
            .build();

    pointRepository.save(point);

    userRepository.updateCurrentPoint(user.getCurrentPoint() + amount);

    return point;
  }
}
