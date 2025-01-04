package com.juny.spacestory.domain.point.common.repository;

import com.juny.spacestory.domain.point.common.entity.Point;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PointRepository {

  List<Point> findAllByUserId(Long userId);

  void save(Point point);
}
