package com.juny.spacestory.domain.space.common.repository;

import com.juny.spacestory.domain.space.common.entity.DetailedSpace;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetailedSpaceRepository {

  Optional<DetailedSpace> findById(Long detailedSpaceId);

  Optional<DetailedSpace> findWithSpaceById(Long detailedSpaceId);
}
