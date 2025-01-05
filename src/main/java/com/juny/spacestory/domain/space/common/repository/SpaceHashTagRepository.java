package com.juny.spacestory.domain.space.common.repository;

import com.juny.spacestory.domain.space.common.entity.SpaceHashtag;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpaceHashTagRepository {

  void saveAll(List<SpaceHashtag> spaceHashtags);
}
