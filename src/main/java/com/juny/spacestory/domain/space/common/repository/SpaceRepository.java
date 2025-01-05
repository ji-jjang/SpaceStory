package com.juny.spacestory.domain.space.common.repository;

import com.juny.spacestory.domain.space.common.entity.Space;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpaceRepository {

  Optional<Space> findByDetailedSpaceId(Long detailedSpaceId);

  void updateSpaceAdvertising(Long id, Boolean advertiseStatus);

  List<Long> findAllDetailedSpaceIdsByHostId(Long userId);

  Optional<Space> findSpaceDetailById(Long spaceId);

  void save(Space space);
}
