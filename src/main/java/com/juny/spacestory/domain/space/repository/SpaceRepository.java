package com.juny.spacestory.domain.space.repository;

import com.juny.spacestory.domain.space.entity.Space;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpaceRepository {

  Optional<Space> findByDetailedSpaceId(Long detailedSpaceId);

  void updateSpaceAdvertising(Long id, Boolean advertiseStatus);

  List<Long> findAllDetailedSpaceIdsByHostId(Long userId);
}
