package com.KievTrung.core.service;

import com.KievTrung.core.dao.PaymentDao;
import com.KievTrung.core.domain.Payment;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.repository.RepositoryI;
import com.KievTrung.util.helper.Validation;

import java.util.List;

// use for debt collection and supplier payment
public class PaymentService implements ServiceI<Payment> {
  RepositoryI<Payment> paymentRepository = new PaymentDao();


  @Override
  public void create(Person.Type type, Payment payment) {
	Validation.validate(payment);
	paymentRepository.create(type, payment);
  }

  @Override
  public void update(Person.Type type, Payment obj) {

  }

  @Override
  public void delete(Person.Type type, Integer id) {

  }

  @Override
  public Payment get(Person.Type type, Integer id) {
	return null;
  }


  @Override
  public List<Payment> getAll(Person.Type type) {
	return paymentRepository.findAll(type);
  }
}

