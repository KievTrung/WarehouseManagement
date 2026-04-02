package com.KievTrung.core.dao;

import com.KievTrung.core.domain.PaymentDetail;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.repository.RepositoryI;
import com.KievTrung.util.helper.DaoHelper;
import lombok.Cleanup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentDetailDao extends DaoHelper<PaymentDetail> implements RepositoryI<PaymentDetail> {

  public List<PaymentDetail> findAllById(Integer paymentId) {
	String sql = "select id, voucherId, debtBeforePrincipal, debtBeforeInterest, debtBeforeTotal, paidInterest, paidPrincipal, remainingPrincipal, remainingInterest, createDate " +
			"from supplier_payment_details " +
			"where paymentId = ?";
	return wrapListPrepareReturnListT(new String[]{sql}, new SqlAction<>() {
	  @Override
	  public List<PaymentDetail> execListPrepareReturnListT(List<PreparedStatement> pss) throws SQLException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		ps.setInt(1, paymentId);
		ResultSet rs = ps.executeQuery();
		List<PaymentDetail> pds = new ArrayList<>();
		while (rs.next()) {
		  PaymentDetail pd = new PaymentDetail(
				  rs.getInt("id"),
				  null,
				  rs.getInt("voucherId"),
				  rs.getBigDecimal("debtBeforePrincipal"),
				  rs.getBigDecimal("debtBeforeInterest"),
				  rs.getBigDecimal("debtBeforeTotal"),
				  rs.getBigDecimal("paidInterest"),
				  rs.getBigDecimal("paidPrincipal"),
				  rs.getBigDecimal("remainingPrincipal"),
				  rs.getBigDecimal("remainingInterest"),
				  rs.getTimestamp("createDate").toLocalDateTime(),
				  null
		  );
		  pds.add(pd);
		}
		return pds;
	  }
	});
  }

  @Override
  public void create(Person.Type type, PaymentDetail obj) {

  }

  @Override
  public void create(Person.Type type, List<PaymentDetail> objs) {

  }

  @Override
  public void update(Person.Type type, PaymentDetail obj) {

  }

  @Override
  public void delete(Person.Type type, Integer id) {

  }

  @Override
  public List<PaymentDetail> findAll(Person.Type type) {
	return List.of();
  }

  @Override
  public List<PaymentDetail> findByName(Person.Type type, String name) {
	return List.of();
  }

  @Override
  public PaymentDetail findById(Person.Type type, Integer id) {
	return null;
  }

  @Override
  public List<PaymentDetail> findAllById(Person.Type type, Integer id) {
	return List.of();
  }

  @Override
  public List<PaymentDetail> findAllActiveById(Person.Type type, Integer id) {
	return List.of();
  }
}
