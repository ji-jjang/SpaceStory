package com.juny.spacestory.domain.space.common.repository;

import com.juny.spacestory.domain.space.common.entity.HashTag;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HashTagRepository {

  Optional<HashTag> findByName(String hashtag);

  void save(HashTag hashtag);
}
