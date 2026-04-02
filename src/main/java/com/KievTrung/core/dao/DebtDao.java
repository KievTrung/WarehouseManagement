package com.KievTrung.core.dao;

import com.KievTrung.core.domain.Debt;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.repository.RepositoryI;
import com.KievTrung.util.helper.DaoHelper;
import lombok.Cleanup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DebtDao extends DaoHelper<Debt> implements RepositoryI<Debt> {


  @Override
  public void create(Person.Type type, Debt obj) {

  }

  @Override
  public void create(Person.Type type, List<Debt> objs) {

  }

  @Override
  public void update(Person.Type type, Debt obj) {

  }

  @Override
  public void delete(Person.Type type, Integer id) {

  }

  @Override
  public List<Debt> findAll(Person.Type type) {
	String sql = String.format("select * from %s_debt order by isSettled desc, createdDate desc", type == Person.Type.CUSTOMER ? "sale" : "purchase");

	return wrapListResultReturnListT(new String[]{sql}, new SqlAction<>() {
	  @Override
	  public List<Debt> execListResultReturnListT(List<ResultSet> rss) throws SQLException {
		@Cleanup ResultSet rs = rss.getFirst();
		List<Debt> debts = new ArrayList<>();
		while (rs.next()) {
		  debts.add(new Debt(
				  rs.getInt("voucherId"),
				  rs.getInt("personId"),
				  rs.getBigDecimal("debtPrincipal"),
				  rs.getBigDecimal("debtInterest"),
				  rs.getBigDecimal("debtTotal"),
				  rs.getDate("createdDate").toLocalDate(),
				  rs.getDate("lastInterestCalculatedDate").toLocalDate(),
				  rs.getBoolean("isSettled")
		  ));
		}
		return debts;
	  }
	});
  }

  @Override
  public List<Debt> findByName(Person.Type type, String name) {
	return List.of();
  }

  @Override
  public Debt findById(Person.Type type, Integer id) {
	return null;
  }

  @Override
  public List<Debt> findAllById(Person.Type type, Integer id) {
	return List.of();
  }

  @Override
  public List<Debt> findAllActiveById(Person.Type type, Integer id) {
	String sql = "select * from purchase_debt where personId = ? and isSettled = 0";

	return wrapListPrepareReturnListT(new String[]{sql}, new SqlAction<>() {
	  @Override
	  public List<Debt> execListPrepareReturnListT(List<PreparedStatement> pss) throws SQLException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		ps.setInt(1, id);
		@Cleanup ResultSet rs = ps.executeQuery();
		List<Debt> debts = new ArrayList<>();
		while (rs.next()) {
		  debts.add(new Debt(
				  rs.getInt("voucherId"),
				  id,
				  rs.getBigDecimal("debtPrincipal"),
				  rs.getBigDecimal("debtInterest"),
				  rs.getBigDecimal("debtTotal"),
				  rs.getDate("createdDate").toLocalDate(),
				  rs.getDate("lastInterestCalculatedDate").toLocalDate(),
				  false
		  ));
		}
		return debts;
	  }
	});
  }
}
