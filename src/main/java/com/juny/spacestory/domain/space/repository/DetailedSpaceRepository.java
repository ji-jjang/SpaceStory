package com.juny.spacestory.domain.space.repository;

import com.juny.spacestory.domain.space.entity.DetailedSpace;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetailedSpaceRepository {

  Optional<DetailedSpace> findById(Long detailedSpaceId);
}
