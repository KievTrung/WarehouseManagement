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
@EqualsAndHashCode(of = "id")
public class Payment {
  private Integer id;
  @PastOrPresent(message = "Ngày thanh toán không hợp lệ")
  private LocalDate paymentDate;
  @NotEmpty(message = "Id cá nhân trống")
  private Integer personId; // FK
  @NotEmpty(message = "Tên cá nhân trống")
  private String personName;
  @DecimalMin(value = "0", message = "Số tiền thanh toán lớn hơn 0")
  private BigDecimal paidAmount;
  private String note;
  private LocalDateTime createDate = LocalDateTime.now();
  private LocalDateTime updateDate = LocalDateTime.now();
  private LocalDateTime deletedAt;
}
