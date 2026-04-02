package com.KievTrung.core.domain;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Item {
  private Integer id;
  @NotEmpty(message = "Item name must not empty")
  private String name;
  @NotEmpty(message = "Unit must not empty")
  private String unit;
//  @NotEmpty(message = "Price empty")
  @DecimalMin(value = "0", message = "Invalid price")
  private BigDecimal salePrice;
  @DecimalMin(value = "0", message = "Invalid quantity")
  private BigDecimal totalQty;
  private String note;
  private LocalDateTime lastUpdateStock;
  private LocalDateTime createDate;
  private LocalDateTime updateDate;

}
