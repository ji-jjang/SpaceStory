package com.juny.spacestory.review.repository;

import com.juny.spacestory.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  Page<Review> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
}
