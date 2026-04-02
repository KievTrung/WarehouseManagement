package com.KievTrung.core.repository;

import com.KievTrung.core.domain.Person;
import com.KievTrung.core.domain.Voucher;
import com.KievTrung.core.domain.VoucherLine;

import java.util.List;

public interface VoucherRepositoryI extends RepositoryI<Voucher> {
  void payLater(Voucher voucher);

  void payNow(Voucher voucher);

  List<VoucherLine> findAllLineById(Person.Type type, Integer id);
}