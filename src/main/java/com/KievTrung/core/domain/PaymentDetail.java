package com.KievTrung.core.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PaymentDetail {
  @NotNull
  private Integer id;
  @NotNull
  private Integer paymentId; // FK
  @NotNull
  private Integer voucherId; // FK
  @NotNull
  private BigDecimal debtBeforePrincipal;
  @NotNull
  private BigDecimal debtBeforeInterest;
  @NotNull
  private BigDecimal debtBeforeTotal;
  @NotNull
  private BigDecimal paidInterest;
  @NotNull
  private BigDecimal paidPrincipal;
  @NotNull
  private BigDecimal remainingPrincipal;
  @NotNull
  private BigDecimal remainingInterest;
  private LocalDateTime createDate = LocalDateTime.now();
  private LocalDateTime updateDate = LocalDateTime.now();
}
