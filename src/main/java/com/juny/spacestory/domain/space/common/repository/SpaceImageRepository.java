package com.juny.spacestory.domain.space.common.repository;

import com.juny.spacestory.domain.space.common.entity.SpaceImage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpaceImageRepository {

  void saveAll(List<SpaceImage> spaceImages);
}
