package com.KievTrung.core.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Voucher {
  public enum Type {ANNUALY, MONTHLY}

  private Integer id;
  @PastOrPresent(message = "Invalid date")
  private LocalDate voucherDate;
  @NotNull(message = "Empty person id")
  private Integer personId; // FK
  @NotEmpty(message = "Empty person name")
  private String personName;
  @NotEmpty(message = "Empty rate")
  @DecimalMin(value = "0", message = "Invalid rate")
  private BigDecimal rate;
  private Type rateType;
  private String note;
  private LocalDateTime createDate = LocalDateTime.now();
  private LocalDateTime updateDate = LocalDateTime.now();
  private LocalDateTime deleteAt;

  private Person.Type personType;

  @NotEmpty(message = "Voucher lines must not empty")
  private Set<VoucherLine> voucherLines;
}
