package com.KievTrung.core.service;

import com.KievTrung.core.domain.Voucher;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InterestCalculationService {
  public static BigDecimal convertSimpleDaily(BigDecimal rate, Voucher.Type type) {
	BigDecimal daysInYear = new BigDecimal(365);
	// daysInMonth = 365 / 12
	BigDecimal daysInMonth = daysInYear.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);

	return rate.divide(type == Voucher.Type.MONTHLY ? daysInMonth : daysInYear, 2, RoundingMode.HALF_UP);
  }
}
