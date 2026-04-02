package com.KievTrung.core.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Person {
  public enum Type {CUSTOMER, SUPPLIER}

  private Integer id;
  @NotNull
  @NotBlank(message = "Name must not blank")
  private String name;
  @Pattern(regexp = "^\\s*$|^0?[0-9]{9}$", message = "Phone number invalid")
  private String phone;
  private String address;
  private String note;
  private LocalDateTime createDate = LocalDateTime.now();
  private LocalDateTime updateDate = LocalDateTime.now();
  private Type type;

  public Boolean isCustomer() {
	return type == Type.CUSTOMER;
  }
}
