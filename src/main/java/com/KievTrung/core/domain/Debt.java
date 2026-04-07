package com.KievTrung.core.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

// Nợ nhà cung cấp và nợ của khách hàng
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "voucherId")
public class Debt implements Comparator<Debt> {
  @NotNull
  private Integer voucherId; // PK FK
  @NotNull
  private Integer personId; // FK
  @NotNull
  private BigDecimal debtPrincipal;
  @NotNull
  private BigDecimal debtInterest;
  @NotNull
  private BigDecimal debtTotal;
  @NotNull
  private LocalDate createdDate;
  @NotNull
  private LocalDate lastInterestCalculatedDate;
  @NotNull
  private Boolean isSettled = false;


  @Override
  public int compare(Debt o1, Debt o2) {
	LocalDate t1 = o1.getCreatedDate();
	LocalDate t2 = o2.getCreatedDate();
	return t1.compareTo(t2);
  }

}
