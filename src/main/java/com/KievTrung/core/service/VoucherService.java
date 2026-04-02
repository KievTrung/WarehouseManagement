package com.KievTrung.core.service;

import com.KievTrung.core.dao.DebtDao;
import com.KievTrung.core.dao.VoucherDao;
import com.KievTrung.core.domain.Debt;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.domain.Voucher;
import com.KievTrung.core.domain.VoucherLine;
import com.KievTrung.core.repository.RepositoryI;
import com.KievTrung.core.repository.VoucherRepositoryI;
import com.KievTrung.util.helper.Validation;

import java.util.List;

public class VoucherService implements ServiceI<Voucher> {
  private final VoucherRepositoryI repo = new VoucherDao();
  private final RepositoryI<Debt> debtRepository = new DebtDao();

  @Override
  public void create(Person.Type type, Voucher purchaseVoucher) {
	Validation.validate(purchaseVoucher);
	repo.payLater(purchaseVoucher);
  }

  @Override
  public void update(Person.Type type, Voucher obj) {

  }

  @Override
  public void delete(Person.Type type, Integer id) {

  }

  public void createAndPayNow(Person.Type type, Voucher purchaseVoucher) {
	// if yes -> display error, cancel creation
	// check if there any debt
	if (!debtRepository.findAllActiveById(type, purchaseVoucher.getPersonId()).isEmpty())
	  throw new RuntimeException("Unpaid debt detected");
	// if no -> create this voucher and create payment immediately without
	Validation.validate(purchaseVoucher);
	repo.payNow(purchaseVoucher);
  }

  public void create(Person.Type type, List<Voucher> vouchers){
	repo.create(type, vouchers);
  }

  @Override
  public Voucher get(Person.Type type, Integer id) {
	return repo.findById(type, id);
  }

  @Override
  public List<Voucher> getAll(Person.Type type) {
	return repo.findAll(type);
  }

  public List<VoucherLine> findAllLineById(Person.Type type, Integer id){
	return repo.findAllLineById(type, id);
  }
}

