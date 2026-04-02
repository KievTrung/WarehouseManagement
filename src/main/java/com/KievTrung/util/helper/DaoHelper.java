package com.KievTrung.util.helper;

import com.KievTrung.core.domain.VoucherLine;
import com.KievTrung.util.db.Database;
import lombok.Cleanup;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoHelper<T> {
  public static class SqlAction<T> {
	public void execTransaction(Connection con) throws SQLException, IOException {
	}

	public void execListPrepare(List<PreparedStatement> pss) throws SQLException, IOException {
	}

	public void execListResult(List<ResultSet> rss) throws SQLException, IOException {
	}

	public <R> List<R> execListPrepareReturnListR(List<PreparedStatement> pss, Class<R> type) throws SQLException, IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
	  return null;
	}

	public List<T> execListResultReturnListT(List<ResultSet> rss) throws SQLException, IOException {
	  return null;
	}

	public List<T> execListPrepareReturnListT(List<PreparedStatement> rss) throws SQLException, IOException {
	  return null;
	}

	public T execListPrepareReturnT(List<PreparedStatement> ps) throws SQLException, IOException {
	  return null;
	}
  }

  public <R> List<R> wrapListPrepareReturnListR(String[] sql, Class<R> type, SqlAction<T> action){
	try {
	  @Cleanup Connection con = Database.getConnection();
	  List<PreparedStatement> pss = new ArrayList<>();
	  for (String s : sql)
		pss.add(con.prepareStatement(s));
	  return action.execListPrepareReturnListR(pss, type);
	} catch (SQLException | RuntimeException | IOException | NoSuchMethodException | InvocationTargetException |
			 InstantiationException | IllegalAccessException e) {
	  throw new RuntimeException(e);
	}
  }

  public void wrapTransaction(SqlAction<T> action) {
	try {
	  @Cleanup Connection con = Database.getConnection();
	  con.setAutoCommit(false);
	  action.execTransaction(con);
	  con.commit();
	} catch (SQLException | RuntimeException | IOException e) {
	  throw new RuntimeException(e);
	}
  }

  public void wrapListPrepare(String[] sql, SqlAction<T> action) {
	try {
	  @Cleanup Connection con = Database.getConnection();
	  List<PreparedStatement> pss = new ArrayList<>();
	  for (String s : sql)
		pss.add(con.prepareStatement(s));
	  action.execListPrepare(pss);
	} catch (SQLException | RuntimeException | IOException e) {
	  throw new RuntimeException(e);
	}
  }

  public void wrapListResult(String[] sql, SqlAction<T> action) {
	try {
	  @Cleanup Connection con = Database.getConnection();
	  List<ResultSet> rss = new ArrayList<>();
	  for (String s : sql)
		rss.add(con.prepareStatement(s).executeQuery());
	  action.execListResult(rss);
	} catch (SQLException | RuntimeException | IOException e) {
	  throw new RuntimeException(e);
	}
  }

  public T wrapListPrepareReturnT(String[] sql, SqlAction<T> action) {
	try {
	  @Cleanup Connection con = Database.getConnection();
	  List<PreparedStatement> pss = new ArrayList<>();
	  for (String s : sql)
		pss.add(con.prepareStatement(s));
	  return action.execListPrepareReturnT(pss);
	} catch (SQLException | RuntimeException | IOException e) {
	  throw new RuntimeException(e);
	}
  }

  public List<T> wrapListResultReturnListT(String[] sql, SqlAction<T> action) {
	try {
	  @Cleanup Connection con = Database.getConnection();
	  List<ResultSet> rss = new ArrayList<>();
	  for (String s : sql)
		rss.add(con.prepareStatement(s).executeQuery());
	  return action.execListResultReturnListT(rss);
	} catch (SQLException | RuntimeException | IOException e) {
	  throw new RuntimeException(e);
	}
  }


  public List<T> wrapListPrepareReturnListT(String[] sql, SqlAction<T> action) {
	try {
	  @Cleanup Connection con = Database.getConnection();
	  List<PreparedStatement> pss = new ArrayList<>();
	  for (String s : sql)
		pss.add(con.prepareStatement(s));
	  return action.execListPrepareReturnListT(pss);
	} catch (SQLException | RuntimeException | IOException e) {
	  throw new RuntimeException(e);
	}
  }

  public static Integer executeAndGetId(PreparedStatement ps) throws SQLException {
	ps.executeUpdate();
	@Cleanup ResultSet rs = ps.getGeneratedKeys();
	rs.next();
	return rs.getInt(1);
  }
}
