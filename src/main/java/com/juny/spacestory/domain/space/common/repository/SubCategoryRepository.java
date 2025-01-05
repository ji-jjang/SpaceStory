package com.juny.spacestory.domain.space.common.repository;

import com.juny.spacestory.domain.space.common.entity.SubCategory;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubCategoryRepository {

  Optional<SubCategory> findById(Long id);
}
