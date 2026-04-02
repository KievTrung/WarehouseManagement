package com.KievTrung.core.service;

import com.KievTrung.core.domain.Person;
import com.KievTrung.core.domain.Voucher;
import lombok.Cleanup;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DebtService {
//  - [ ] calculateInterest(debtPrincipal, annualRate, dayOverdue) → decimal
//  - [ ] updateDebtInterest(voucherNo) → recalc interest
//  - [ ] fullyDebtByVoucher(voucherNo) → bool

  public static BigDecimal calculateInterestTillNow(BigDecimal debtPrincipal,
													BigDecimal rate,
													Voucher.Type rateType,
													LocalDate start) {
	BigDecimal dayOverDue = new BigDecimal(ChronoUnit.DAYS.between(start, LocalDate.now()));
	// interest = debtPrincipal x dailyRate x dayOverDue
	return debtPrincipal
			.multiply(InterestCalculationService.convertSimpleDaily(rate, rateType))
			.multiply(dayOverDue);
  }

  public static PreparedStatement updateDebtInterest(Connection con, boolean isCustomer, Integer voucherId) throws SQLException {
	String debtTable = isCustomer ? "sale" : "purchase";
	// get debt info of voucher
	@Cleanup PreparedStatement ps = con.prepareStatement(
			"select debtPrinciple, rate, rateType, lastInterestCalculatedDate as lastTime " +
					String.format("from %s_vouchers pv inner join %s_debt pd on pd.voucherId = pv.id ", debtTable, debtTable) +
					"where pv.id = ?"
	);
	// update interest
	PreparedStatement psImodify = con.prepareStatement(
			String.format("update %s_debt ", debtTable) +
					"set debtInterest = debtInterest + ?, lastInterestCalculatedDate = ? " +
					"where voucherId = ?"
	);
	// update interest
	PreparedStatement psI = con.prepareStatement(
			String.format("update %s_debt set lastInterestCalculatedDate = ? where voucherId = ?", debtTable)
	);
	ps.setInt(1, voucherId);
	ResultSet rs = ps.executeQuery();
	rs.next();
	BigDecimal debtPrinciple = rs.getBigDecimal("debtPrinciple");
	BigDecimal rate = rs.getBigDecimal("rate");
	Voucher.Type type = Voucher.Type.valueOf(rs.getString("rateType"));
	LocalDate lastTime = rs.getDate("lastTime").toLocalDate();

	if (rate.compareTo(BigDecimal.ZERO) > 0) {
	  //calculate interest based on the last time paid
	  BigDecimal interest = calculateInterestTillNow(debtPrinciple, rate, type, lastTime);

	  psImodify.setBigDecimal(1, interest);
	  psImodify.setDate(2, Date.valueOf(LocalDate.now()));
	  psImodify.setInt(3, voucherId);
	  return psImodify;
	}
	else {
	  psI.setDate(1, Date.valueOf(LocalDate.now()));
	  psI.setInt(2, voucherId);
	  return psI;
	}
  }
}
