package com.juny.spacestory.domain.user.repository;

import com.juny.spacestory.domain.user.entity.User;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository {

  Optional<User> findById(Long userId);
}
