package com.KievTrung.core.dao;

import com.KievTrung.core.domain.Person;
import com.KievTrung.core.domain.Voucher;
import com.KievTrung.core.domain.VoucherLine;
import com.KievTrung.core.repository.VoucherRepositoryI;
import com.KievTrung.util.helper.DaoHelper;
import lombok.Cleanup;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VoucherDao extends DaoHelper<Voucher> implements VoucherRepositoryI {
  public Integer createVoucher(Connection con, Voucher voucher, boolean isCustomer) throws SQLException {

	// add voucher
	@Cleanup PreparedStatement psVoucher = con.prepareStatement(
			"insert into _vouchers(personId, personName, note, createDate, updateDate, deletedAt, voucherDate, rateType, rate) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
					.replaceFirst("_", isCustomer ? "sale_" : "purchase_")
	);
	psVoucher.setInt(1, voucher.getPersonId());
	psVoucher.setString(2, voucher.getPersonName());
	psVoucher.setString(3, voucher.getNote());
	psVoucher.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
	psVoucher.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
	psVoucher.setString(6, null);
	psVoucher.setDate(7, Date.valueOf(LocalDate.now()));

	//add interest
	psVoucher.setString(8, voucher.getPersonType().toString());
	psVoucher.setBigDecimal(9, voucher.getRate());

	int voucherId = DaoHelper.executeAndGetId(psVoucher);

	// update item, voucherline
	@Cleanup PreparedStatement psItemInsert = con.prepareStatement(
			"insert into items(name, unit, salePrice, totalQty, note, lastUpdateStock, createDate, updateDate) values (?, ?, ?, ?, ?, ?, ?, ?)"
	);
	@Cleanup PreparedStatement psItemUpdate = con.prepareStatement(
			"update items set totalQty = totalQty _ ?, lastUpdateStock = ? where id = ? ".replaceFirst("_", isCustomer ? "-" : "+")
	);
	@Cleanup PreparedStatement psLine = con.prepareStatement(
			"insert into _voucher_lines(voucherId, itemId, itemName, unit, qty, unitCost, lineAmount, createDate, updateDate) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
					.replaceFirst("_", isCustomer ? "sale_" : "purchase_")
	);
	for (VoucherLine line : voucher.getVoucherLines()) {
	  Integer itemId = line.getItemId();
	  PreparedStatement psItem = itemId == null && !isCustomer ? psItemInsert : psItemUpdate;
	  if (itemId == null) {
		psItem.setString(1, line.getItemName());
		psItem.setString(2, line.getUnit());
		psItem.setBigDecimal(3, BigDecimal.ZERO);
		psItem.setBigDecimal(4, line.getQty());
		psItem.setString(5, "");
		psItem.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
		psItem.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
		psItem.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

		itemId = DaoHelper.executeAndGetId(psItem);
	  } else {
		psItem.setBigDecimal(1, line.getQty());
		psItem.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
		psItem.setInt(3, line.getItemId());
		psItem.executeUpdate();
	  }
	  psLine.setInt(1, voucherId);
	  psLine.setInt(2, itemId);
	  psLine.setString(3, line.getItemName());
	  psLine.setString(4, line.getUnit());
	  psLine.setBigDecimal(5, line.getQty());
	  psLine.setBigDecimal(6, line.getUnitCost());
	  psLine.setBigDecimal(7, line.getLineAmount());
	  psLine.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
	  psLine.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

	  psLine.executeUpdate();
	}
	return voucherId;
  }

  @Override
  public void payLater(Voucher voucher) {
	wrapTransaction(new SqlAction<>() {
	  @Override
	  public void execTransaction(Connection con) throws SQLException {
		boolean isCustomer = voucher.getPersonType() == Person.Type.CUSTOMER;

		Integer voucherId = createVoucher(con, voucher, isCustomer);

		// create debt record
		@Cleanup PreparedStatement psD = con.prepareStatement(
				"insert into _debt values (?, ?, ?, ?, ?, ?, ?, ?)".replaceFirst("_", isCustomer ? "sale_" : "purchase_")
		);
		BigDecimal debtPrinciple = voucher.getVoucherLines().stream()
				.map(VoucherLine::getLineAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		psD.setInt(1, voucherId);
		psD.setInt(2, voucher.getPersonId());
		psD.setBigDecimal(3, debtPrinciple);
		psD.setBigDecimal(4, BigDecimal.ZERO);
		psD.setBigDecimal(5, debtPrinciple);
		psD.setDate(6, Date.valueOf(voucher.getVoucherDate()));
		psD.setDate(7, Date.valueOf(voucher.getVoucherDate()));
		psD.setBoolean(8, false);

		psD.executeUpdate();
	  }
	});
  }

  @Override
  public void payNow(Voucher voucher) {
	wrapTransaction(new SqlAction<>() {
	  @Override
	  public void execTransaction(Connection con) throws SQLException {
		boolean isCustomer = voucher.getPersonType() == Person.Type.CUSTOMER;

		Integer voucherId = createVoucher(con, voucher, isCustomer);

		BigDecimal debtPrinciple = voucher.getVoucherLines().stream()
				.map(VoucherLine::getLineAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		Timestamp create = Timestamp.valueOf(voucher.getCreateDate());
		Timestamp update = Timestamp.valueOf(voucher.getUpdateDate());

		// create payment
		@Cleanup PreparedStatement psPayment = con.prepareStatement(
				"insert into supplier_payments (paymentDate, personId, personName, paidAmount, note, createDate, updateDate, deletedAt) values (?,?,?,?,?,?,?,?)"
		);
		psPayment.setDate(1, Date.valueOf(voucher.getVoucherDate()));
		psPayment.setInt(2, voucher.getPersonId());
		psPayment.setString(3, voucher.getPersonName());
		psPayment.setBigDecimal(4, debtPrinciple);
		psPayment.setString(5, voucher.getNote());
		psPayment.setTimestamp(6, create);
		psPayment.setTimestamp(7, update);
		psPayment.setTimestamp(8, null);

		int paymentId = DaoHelper.executeAndGetId(psPayment);
		// create payment detail
		@Cleanup PreparedStatement psA = con.prepareStatement(
				"insert into supplier_payment_details (paymentId, voucherId, debtBeforePrincipal, debtBeforeInterest, debtBeforeTotal, paidInterest, paidPrincipal, remainingPrincipal, remainingInterest, createDate, updateDate) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		);
		psA.setInt(1, paymentId);
		psA.setInt(2, voucherId);
		psA.setBigDecimal(3, debtPrinciple);
		psA.setBigDecimal(4, BigDecimal.ZERO);
		psA.setBigDecimal(5, debtPrinciple);
		psA.setBigDecimal(6, BigDecimal.ZERO);
		psA.setBigDecimal(7, debtPrinciple);
		psA.setBigDecimal(8, BigDecimal.ZERO);
		psA.setBigDecimal(9, BigDecimal.ZERO);
		psA.setTimestamp(10, create);
		psA.setTimestamp(11, update);

		psA.executeUpdate();
	  }
	});
  }


  @Override
  public List<VoucherLine> findAllLineById(Person.Type type, Integer id) {
	String sql = "select * from _voucher_lines where voucherId = ?"
			.replaceFirst("_", type == Person.Type.CUSTOMER ? "sale_" : "purchase_");

	return wrapListPrepareReturnListR(new String[]{sql}, VoucherLine.class, new SqlAction<>(){
	  @Override
	  public <R> List<R> execListPrepareReturnListR(List<PreparedStatement> pss, Class<R> classType) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		ps.setInt(1, id);
		@Cleanup ResultSet rs = ps.executeQuery();

		List<R> Rs = new ArrayList<>(); // voucher lines
		while(rs.next()){
		  Rs.add(classType.getDeclaredConstructor(
				  Integer.class,
				  Integer.class,
				  Integer.class,
				  String.class,
				  String.class,
				  BigDecimal.class,
				  BigDecimal.class,
				  BigDecimal.class,
				  LocalDateTime.class,
				  LocalDateTime.class
		  ).newInstance(
				  rs.getInt("id"),
				  rs.getInt("voucherId"),
				  rs.getInt("itemId"),
				  rs.getString("itemName"),
				  rs.getString("unit"),
				  rs.getBigDecimal("qty"),
				  rs.getBigDecimal(type ==  Person.Type.CUSTOMER ? "unitPrice" : "unitCost"),
				  rs.getBigDecimal("lineAmount"),
				  rs.getTimestamp("createDate").toLocalDateTime(),
				  rs.getTimestamp("updateDate").toLocalDateTime()
		  ));
		}
		return Rs;
	  }
	});
  }

  @Override
  public List<Voucher> findAll(Person.Type type) {
	boolean isCustomer = type == Person.Type.CUSTOMER;

	String sql = "select id, personId, personName, note, rateType, rate, createDate cD, updateDate uD, deletedAt, voucherDate " +
			String.format("from %s_vouchers ", isCustomer ? "sale" : "purchase");

	return wrapListResultReturnListT(new String[]{sql}, new SqlAction<>() {
	  @Override
	  public List<Voucher> execListResultReturnListT(List<ResultSet> rss) throws SQLException {
		ResultSet rs = rss.getFirst();
		List<Voucher> vouchers = new ArrayList<>();
		while (rs.next()) {
		  Voucher voucher = new Voucher();
		  voucher.setId(rs.getInt("id"));
		  voucher.setPersonId(rs.getInt("personId"));
		  voucher.setPersonName(rs.getString("personName"));

		  voucher.setRateType(Voucher.Type.valueOf(rs.getString("rateType")));
		  voucher.setRate(rs.getBigDecimal("rate"));

		  voucher.setNote(rs.getString("note"));
		  voucher.setCreateDate(rs.getTimestamp("cD").toLocalDateTime());
		  voucher.setUpdateDate(rs.getTimestamp("uD").toLocalDateTime());
		  voucher.setVoucherDate(rs.getDate("voucherDate").toLocalDate());
		  Timestamp delete = rs.getTimestamp("deletedAt");
		  voucher.setDeleteAt(delete == null ? null : delete.toLocalDateTime());

		  vouchers.add(voucher);
		}
		return vouchers;
	  }
	});
  }

  @Override
  public void create(Person.Type type, Voucher obj) {

  }

  @Override
  public void create(Person.Type type, List<Voucher> vouchers) {
	wrapTransaction(new SqlAction<>() {
	  @Override
	  public void execTransaction(Connection con) throws SQLException {

		boolean isCustomer = type == Person.Type.CUSTOMER;
		// insert voucher
		@Cleanup PreparedStatement psVoucher = con.prepareStatement(
				String.format("insert into %s_vouchers(id, personId, personName, rate, rateType, note, createDate, updateDate, deletedAt, voucherDate) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", isCustomer ? "sale" : "purchase")
		);
		// update item
		@Cleanup PreparedStatement psItemUpdate = con.prepareStatement(
				String.format("update items set totalQty = totalQty %s ?, lastUpdateStock = ? where id = ? ", isCustomer ? "-" : "+")
		);
		// insert voucherline
		@Cleanup PreparedStatement psLine = con.prepareStatement(
				String.format("insert into %s_voucher_lines(voucherId, itemId, itemName, unit, qty, unitCost, lineAmount, createDate, updateDate) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", isCustomer ? "sale" : "purchase")
		);
		// insert debt record
		@Cleanup PreparedStatement psD = con.prepareStatement(
				String.format("insert into %s_debt values (?, ?, ?, ?, ?, ?, ?, ?)", isCustomer ? "sale" : "purchase")
		);
		for (Voucher voucher : vouchers){
		  psVoucher.setInt(1, voucher.getId());
		  psVoucher.setInt(2, voucher.getPersonId());
		  psVoucher.setString(3, voucher.getPersonName());
		  psVoucher.setBigDecimal(4, voucher.getRate());
		  psVoucher.setString(5, voucher.getRateType().toString());
		  psVoucher.setString(6, voucher.getNote());
		  psVoucher.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
		  psVoucher.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
		  psVoucher.setString(9, null);
		  psVoucher.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
		  psVoucher.addBatch();

		  for (VoucherLine line : voucher.getVoucherLines()) {
			Integer itemId = line.getItemId();

			psItemUpdate.setBigDecimal(1, line.getQty());
			psItemUpdate.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
			psItemUpdate.setInt(3, line.getItemId());
			psItemUpdate.addBatch();

			psLine.setInt(1, voucher.getId());
			psLine.setInt(2, itemId);
			psLine.setString(3, line.getItemName());
			psLine.setString(4, line.getUnit());
			psLine.setBigDecimal(5, line.getQty());
			psLine.setBigDecimal(6, line.getUnitCost());
			psLine.setBigDecimal(7, line.getLineAmount());
			psLine.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
			psLine.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

			psLine.addBatch();
		  }
		  BigDecimal debtPrinciple = voucher.getVoucherLines().stream()
				  .map(VoucherLine::getLineAmount)
				  .reduce(BigDecimal.ZERO, BigDecimal::add);
		  psD.setInt(1, voucher.getId());
		  psD.setInt(2, voucher.getPersonId());
		  psD.setBigDecimal(3, debtPrinciple);
		  psD.setBigDecimal(4, BigDecimal.ZERO);
		  psD.setBigDecimal(5, debtPrinciple);
		  psD.setDate(6, Date.valueOf(voucher.getVoucherDate()));
		  psD.setDate(7, Date.valueOf(voucher.getVoucherDate()));
		  psD.setBoolean(8, false);

		  psD.addBatch();
		}
		psVoucher.executeBatch();
		psItemUpdate.executeBatch();
		psLine.executeBatch();
		psD.executeBatch();
	  }
	});
  }

  @Override
  public void update(Person.Type type, Voucher obj) {

  }

  @Override
  public void delete(Person.Type type, Integer id) {

  }

  @Override
  public List<Voucher> findByName(Person.Type type, String name) {
	return List.of();
  }

  public Voucher findById(Person.Type type, Integer id) {
	boolean isCustomer = type == Person.Type.CUSTOMER;
	String sqlVoucher = "select * from _vouchers where id = ?".replaceFirst("_", isCustomer ? "sale_" : "purchase_");
	String sqlVoucherLine = "select * from _voucher_lines where voucherId = ?".replaceFirst("_", isCustomer ? "sale_" : "purchase_");

	return wrapListPrepareReturnT(new String[]{sqlVoucher, sqlVoucherLine}, new SqlAction<>() {
	  @Override
	  public Voucher execListPrepareReturnT(List<PreparedStatement> pss) throws SQLException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		// get voucher
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();
		rs.next();

		Timestamp delete = rs.getTimestamp("deletedAt");
		Voucher voucher = new Voucher(
				id,
				rs.getDate("voucherDate").toLocalDate(),
				rs.getInt("personId"),
				rs.getString("personName"),
				rs.getBigDecimal("rate"),
				Voucher.Type.valueOf(rs.getString("rateType")),
				rs.getString("note"),
				rs.getTimestamp("createDate").toLocalDateTime(),
				rs.getTimestamp("updateDate").toLocalDateTime(),
				delete == null ? null : delete.toLocalDateTime(),
				type,
				null
		);

		// get voucher line
		@Cleanup PreparedStatement psLine = pss.getFirst();

		psLine.setInt(1, id);
		ResultSet rsLine = psLine.executeQuery();
		Set<VoucherLine> voucherLines = new HashSet<>();
		while (rsLine.next()) {
		  VoucherLine pvl = new VoucherLine();
		  pvl.setId(rsLine.getInt("id"));
		  pvl.setItemId(rs.getInt("itemId"));
		  pvl.setItemName(rs.getString("itemName"));
		  pvl.setUnit(rs.getString("unit"));
		  pvl.setQty(rs.getBigDecimal("qty"));
		  pvl.setUnitCost(rs.getBigDecimal("unitCost"));
		  pvl.setLineAmount(rs.getBigDecimal("lineAmount"));
		  voucherLines.add(pvl);
		}
		voucher.setVoucherLines(voucherLines);
		return voucher;
	  }
	});
  }

  @Override
  public List<Voucher> findAllById(Person.Type type, Integer id) {
	return List.of();
  }

  @Override
  public List<Voucher> findAllActiveById(Person.Type type, Integer id) {
	return List.of();
  }
}
