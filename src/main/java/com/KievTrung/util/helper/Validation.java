package com.KievTrung.util.helper;

import jakarta.validation.*;

import java.util.Set;

public class Validation {
  private static final ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();

  public static <T> void validate(T obj) {
	Validator validator = factory.getValidator();
	Set<ConstraintViolation<T>> violations = validator.validate(obj);

	if (violations.isEmpty()) return;
	String msg = "";
	for (ConstraintViolation<T> t : violations) {
	  msg += t.getMessage() + "\n";
	}
	throw new RuntimeException(msg);
  }
}
