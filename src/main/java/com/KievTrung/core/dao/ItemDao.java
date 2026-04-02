package com.KievTrung.core.dao;

import com.KievTrung.core.domain.Item;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.repository.RepositoryI;
import com.KievTrung.util.helper.DaoHelper;
import com.KievTrung.util.helper.Validation;
import lombok.Cleanup;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDao extends DaoHelper<Item> implements RepositoryI<Item> {
  @Override
  public void create(Person.Type type/*ignore*/, Item item) {
	try {
	  Validation.validate(item);
	} catch (RuntimeException e) {
	  throw new RuntimeException(e);
	}
	wrapListPrepare(new String[]{"insert into items values (?, ?, ?, ?, ?, ?, ?, ?)"}, new SqlAction<>() {
	  @Override
	  public void execListPrepare(List<PreparedStatement> pss) throws SQLException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		ps.setString(1, item.getName());
		ps.setString(2, item.getUnit());
		ps.setBigDecimal(3, item.getSalePrice());
		ps.setBigDecimal(4, item.getTotalQty());
		ps.setString(5, item.getNote());
		ps.setTimestamp(6, Timestamp.valueOf(item.getLastUpdateStock()));
		ps.setTimestamp(7, Timestamp.valueOf(item.getCreateDate()));
		ps.setTimestamp(8, Timestamp.valueOf(item.getUpdateDate()));
		ps.executeUpdate();
	  }
	});
  }

  @Override
  public void create(Person.Type type /*ignore*/, List<Item> items) {
	wrapTransaction(new SqlAction<>(){
	  @Override
	  public void execTransaction(Connection con) throws SQLException{
		@Cleanup PreparedStatement ps = con.prepareStatement("insert into items values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		for (Item item : items){
		  ps.setInt(1, item.getId());
		  ps.setString(2, item.getName());
		  ps.setString(3, item.getUnit());
		  ps.setBigDecimal(4, item.getSalePrice());
		  ps.setBigDecimal(5, item.getTotalQty());
		  ps.setString(6, item.getNote());
		  ps.setTimestamp(7, Timestamp.valueOf(item.getLastUpdateStock()));
		  ps.setTimestamp(8, Timestamp.valueOf(item.getCreateDate()));
		  ps.setTimestamp(9, Timestamp.valueOf(item.getUpdateDate()));

		  ps.addBatch();
		}
		ps.executeBatch();
	  }
	});
  }

	// todo
  @Override
  public void update(Person.Type type/*ignore*/, Item item) {
	wrapListPrepare(new String[]{"update items set name = ?, unit = ?, salePrice = ?, totalQty = ?, note = ?, updateDate = ? where id = ?"}, new SqlAction<>() {
	  @Override
	  public void execListPrepare(List<PreparedStatement> pss) throws SQLException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		ps.setString(1, item.getName());
		ps.setString(2, item.getUnit());
		ps.setBigDecimal(3, item.getSalePrice());
		ps.setBigDecimal(4, item.getTotalQty());
		ps.setString(5, item.getNote());
		ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
		ps.setInt(7, item.getId());
		ps.executeUpdate();
	  }
	});
  }

  @Override
  public void delete(Person.Type type/*ignore*/, Integer id) {
	wrapListPrepare(new String[]{"delete from items where id = ?"}, new SqlAction<>() {
	  @Override
	  public void execListPrepare(List<PreparedStatement> pss) throws SQLException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		ps.setInt(1, id);
		ps.executeUpdate();
	  }
	});
  }

  @Override
  public List<Item> findAll(Person.Type type/*ignore*/) {
	return wrapListResultReturnListT(new String[]{"select * from items"}, new SqlAction<>() {
	  @Override
	  public List<Item> execListResultReturnListT(List<ResultSet> rss) throws SQLException {
		@Cleanup ResultSet rs = rss.getFirst();
		List<Item> items = new ArrayList<>();
		while (rs.next()) {
		  Item item = new Item(
				  rs.getInt("id"),
				  rs.getString("name"),
				  rs.getString("unit"),
				  rs.getBigDecimal("salePrice"),
				  rs.getBigDecimal("totalQty"),
				  rs.getString("note"),
				  rs.getTimestamp("lastUpdateStock").toLocalDateTime(),
				  rs.getTimestamp("createDate").toLocalDateTime(),
				  rs.getTimestamp("updateDate").toLocalDateTime()
		  );
		  items.add(item);
		}
		return items;
	  }
	});
  }

  @Override
  public List<Item> findByName(Person.Type type/*ignore*/, String name) {
	return wrapListPrepareReturnListT(new String[]{"select * from items where name like ?"}, new SqlAction<>() {
	  @Override
	  public List<Item> execListPrepareReturnListT(List<PreparedStatement> pss) throws SQLException {
		@Cleanup PreparedStatement ps = pss.getFirst();
		ps.setString(1, "%" + name + "%");
		@Cleanup ResultSet rs = ps.executeQuery();
		List<Item> items = new ArrayList<>();
		while (rs.next()) {
		  Item item = new Item(
				  rs.getInt("id"),
				  rs.getString("name"),
				  rs.getString("unit"),
				  rs.getBigDecimal("salePrice"),
				  rs.getBigDecimal("totalQty"),
				  rs.getString("note"),
				  rs.getTimestamp("lastUpdateStock").toLocalDateTime(),
				  rs.getTimestamp("createDate").toLocalDateTime(),
				  rs.getTimestamp("updateDate").toLocalDateTime()
		  );
		  items.add(item);
		}
		return items;
	  }
	});
  }

  @Override
  public Item findById(Person.Type type/*ignore*/, Integer id) {
	return null;
  }

  @Override
  public List<Item> findAllById(Person.Type type/*ignore*/, Integer id) {
	return List.of();
  }

  @Override
  public List<Item> findAllActiveById(Person.Type type/*ignore*/, Integer id) {
	return List.of();
  }
}
