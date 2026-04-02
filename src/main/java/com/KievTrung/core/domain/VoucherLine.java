package com.KievTrung.core.domain;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "itemName")
public class VoucherLine {
  private Integer id;
  @NotNull(message = "Invalid voucher id")
  private Integer voucherId; // FK
  @NotNull(message = "Invalid item id")
  private Integer itemId; // FK
  @NotEmpty(message = "Item name must not empty")
  private String itemName;
  @NotEmpty(message = "Unit must not empty")
  private String unit;
  @NotNull(message = "Quantity required")
  @DecimalMin(value = "0.01", message = "Invalid quantity")
  private BigDecimal qty;
  @NotNull(message = "unit value required")
  @DecimalMin(value = "0", message = "Invalid unit cost")
  private BigDecimal unitCost; // or UnitPrice
  private BigDecimal lineAmount;
  private LocalDateTime createDate;
  private LocalDateTime updateDate;
}
