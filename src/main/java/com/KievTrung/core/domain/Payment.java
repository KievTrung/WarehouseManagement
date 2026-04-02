package com.KievTrung.core.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Payment {
  private Integer id;
  @NotNull
  private LocalDate paymentDate;
  @NotNull
  private Integer personId; // FK
  @NotNull
  private String personName;
  @NotNull
  private BigDecimal paidAmount;
  private String note;
  private LocalDateTime createDate = LocalDateTime.now();
  private LocalDateTime updateDate = LocalDateTime.now();
  private LocalDateTime deletedAt;
}
