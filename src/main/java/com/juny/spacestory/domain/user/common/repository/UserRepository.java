package com.juny.spacestory.domain.user.common.repository;

import com.juny.spacestory.domain.user.common.entity.User;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository {

  Optional<User> findById(Long userId);

  void updateCurrentPoint(int currentPoint);
}
