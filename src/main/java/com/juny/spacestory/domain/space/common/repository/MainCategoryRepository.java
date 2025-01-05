package com.juny.spacestory.domain.space.common.repository;

import com.juny.spacestory.domain.space.common.entity.MainCategory;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MainCategoryRepository {

  Optional<MainCategory> findById(Long id);
}
