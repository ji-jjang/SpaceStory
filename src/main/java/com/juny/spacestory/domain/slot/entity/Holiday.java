package com.juny.spacestory.domain.slot.entity;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class Holiday {

  public static List<LocalDate> holidays =
      List.of(
          LocalDate.of(2025, 1, 1),
          LocalDate.of(2025, 1, 28),
          LocalDate.of(2025, 1, 29),
          LocalDate.of(2025, 1, 30),
          LocalDate.of(2025, 3, 1),
          LocalDate.of(2025, 3, 3),
          LocalDate.of(2025, 5, 5),
          LocalDate.of(2025, 5, 5),
          LocalDate.of(2025, 5, 6),
          LocalDate.of(2025, 6, 6),
          LocalDate.of(2025, 8, 15),
          LocalDate.of(2025, 10, 3),
          LocalDate.of(2025, 10, 5),
          LocalDate.of(2025, 10, 6),
          LocalDate.of(2025, 10, 7),
          LocalDate.of(2025, 10, 8),
          LocalDate.of(2025, 10, 9),
          LocalDate.of(2025, 12, 25));
}
