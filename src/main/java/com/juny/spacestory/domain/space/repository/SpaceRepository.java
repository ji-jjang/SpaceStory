package com.juny.spacestory.domain.space.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpaceRepository {

  void updateSpaceAdvertising(Long id, Boolean advertiseStatus);
}
