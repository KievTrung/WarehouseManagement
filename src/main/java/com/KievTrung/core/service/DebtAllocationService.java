package com.KievTrung.core.service;

import com.KievTrung.core.domain.Debt;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DebtAllocationService {
//  - SupplierDebtAllocationService.java
//  - [ ] allocatePaidAmount(paidAmount, debts) → allocation[]
//    * Quy tắc: Trả lãi trước, gốc để sau
//    * Return: { voucherId, paidInterest, paidPrincipal, remainingInterest, remainingPrincipal }

  @AllArgsConstructor
  public static class Allocation {
	public Integer voucherId;
	public BigDecimal debtPrincipleBeforeApply;
	public BigDecimal debtInterestBeforeApply;
	public BigDecimal debtTotalBeforeApply;
	public BigDecimal paidInterest;
	public BigDecimal paidPrincipal;
	public BigDecimal remainingInterest;
	public BigDecimal remainingPrincipal;
	public boolean isSettled;
  }

  public static List<Allocation> debtAllocate(BigDecimal payAmount, List<Debt> debt) {
	List<Allocation> allocations = new ArrayList<>();
	for (Debt d : debt) {
	  Integer id = d.getVoucherId();
	  BigDecimal interest = d.getDebtInterest();
	  BigDecimal principle = d.getDebtPrincipal();
	  BigDecimal paidInterest;
	  BigDecimal paidPrincipal;
	  BigDecimal remainingPrincipal;
	  BigDecimal remainingInterest;
	  BigDecimal zero = new BigDecimal(0);

	  // after interest
	  payAmount = payAmount.subtract(interest);
	  int enough = payAmount.compareTo(zero);
	  // more than enough to paid interest
	  if (enough > 0) {
		// interest wipe off
		paidInterest = interest;
		remainingInterest = zero;
		// after principle
		payAmount = payAmount.subtract(interest);
		int enough2 = payAmount.compareTo(zero);
		// more than enough2 to paid principle
		if (enough2 >= 0) {
		  // principle wipe off
		  paidPrincipal = principle;
		  remainingPrincipal = zero;
		}
		// not enough to paid principle fully
		else {
		  // payAmount < 0 = - remaining principle
		  paidPrincipal = principle.add(payAmount);
		  remainingPrincipal = payAmount.negate();
		}
	  }
	  // only enough to paid interest
	  else if (enough == 0) {
		paidInterest = interest;
		paidPrincipal = zero;
		remainingPrincipal = principle;
		remainingInterest = zero;
	  }
	  // not enough to paid interest fully
	  else {
		// payAmount < 0 = - remaining interest
		paidInterest = interest.add(payAmount);
		paidPrincipal = zero;
		remainingPrincipal = principle;
		remainingInterest = payAmount.negate();
	  }
	  boolean isSettle = remainingPrincipal.compareTo(zero) == 0;
	  allocations.add(
			  new Allocation(
					  id,
					  principle,
					  interest,
					  principle.add(interest),
					  paidInterest,
					  paidPrincipal,
					  remainingInterest,
					  remainingPrincipal,
					  isSettle));

	  if (payAmount.compareTo(zero) <= 0)
		break;
	}
	return allocations;
  }
}
