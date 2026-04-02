package com.KievTrung.core.service;

import com.KievTrung.core.domain.Person;
import com.KievTrung.core.domain.Voucher;
import com.KievTrung.util.helper.DaoHelper;
import lombok.Cleanup;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ExcelExportService extends DaoHelper<Object> {

  public void createHeader(ResultSet rs, Sheet sheet) throws SQLException {
	ResultSetMetaData metaData = rs.getMetaData();
	int colCount = metaData.getColumnCount();
	Row header = sheet.createRow(0);
	for (int col = 1; col <= colCount; col++) {
	  Cell headerCell = header.createCell(col - 1);
	  headerCell.setCellValue(metaData.getColumnName(col));
	}
  }

  public void exportDebtReport(Person.Type type, String filePath) {
	boolean isCustomer = type == Person.Type.CUSTOMER;
	String personTable = type.toString().toLowerCase();
	String debtTable = isCustomer ? "sale" : "purchase";
	String sqlDebt = String.format("select p.id id, p.name name, sum(debtPrincipal) totalDebtPrincipal, sum(debtInterest) totalDebtInterest, sum(debtTotal) totalDebtAmount " +
			"from %ss p inner join %s_debt sd on p.id = sd.personId " +
			"where sd.isSettled = 0 " +
			"group by id, name " +
			"order by name asc", personTable, debtTable);
	String sqlDebtDetail = String.format("select sv.id voucherId, sv.voucherDate voucherDate, sv.personId personId, sv.personName personName, debtPrincipal, debtInterest, debtTotal, createDate, lastInterestCalculatedDate lastTime" +
			"from %s_debt sd inner join sale_vouchers sv on sd.voucherId = sv.id " +
			"where isSettled = 0 " +
			"order by pN asc", debtTable);
	wrapListResult(new String[]{sqlDebt, sqlDebtDetail}, new SqlAction<>() {
	  @Override
	  public void execListResult(List<ResultSet> rss) throws SQLException, IOException {
		@Cleanup Workbook workbook = new XSSFWorkbook();
		@Cleanup ResultSet rsDebt = rss.getFirst();
		@Cleanup ResultSet rsDebtDetail = rss.getLast();
		// create sheet
		Sheet sheetDebt = workbook.createSheet("debt-by-" + personTable);
		Sheet sheetDebtDetail = workbook.createSheet("debt-detail");
		// create header
		createHeader(rsDebt, sheetDebt);
		createHeader(rsDebtDetail, sheetDebtDetail);
		// create row
		int rowNum = 1;
		while (rsDebt.next()) {
		  Row row = sheetDebt.createRow(rowNum++);
		  int colNum = 0;
		  Cell cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebt.getInt("id"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebt.getString("name"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebt.getBigDecimal("totalDebtPrincipal").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebt.getBigDecimal("totalDebtInterest").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebt.getBigDecimal("totalDebtAmount").toPlainString());
		}
		rowNum = 1;
		while (rsDebtDetail.next()) {
		  Row row = sheetDebtDetail.createRow(rowNum++);
		  int colNum = 0;
		  Cell cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebtDetail.getInt("voucherId"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebtDetail.getDate("voucherDate"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebtDetail.getInt("personId"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebtDetail.getString("personName"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebtDetail.getBigDecimal("debtPrincipal").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebtDetail.getBigDecimal("debtInterest").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsDebtDetail.getBigDecimal("debtTotal").toPlainString());
		  cell = row.createCell(colNum++);
		  long daysOverdue = ChronoUnit.DAYS.between(
				  rsDebtDetail.getDate("createDate").toLocalDate(),
				  rsDebtDetail.getDate("lastTime").toLocalDate()
		  );
		  cell.setCellValue(daysOverdue);
		}
		// save file
		@Cleanup FileOutputStream outputStream = new FileOutputStream(Paths.get(filePath, personTable + "-debt-report.xlsx").toString());
		workbook.write(outputStream);
	  }
	});
  }

  public void exportStockReport(String filePath) {
	String sql = "select id, name, unit, salePrice, totalQty, (salePrice * totalQty) as stockValue from items";
	wrapListResult(new String[]{sql}, new SqlAction<>() {
	  @Override
	  public void execListResult(List<ResultSet> rss) throws SQLException, IOException {
		ResultSet rs = rss.getFirst();

		@Cleanup Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("stock-summary");
		createHeader(rs, sheet);

		int rowNum = 1;
		while (rs.next()) {
		  Row row = sheet.createRow(rowNum++);
		  int colNum = 0;
		  Cell cell = row.createCell(colNum++);
		  cell.setCellValue(rs.getInt("id"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rs.getInt("name"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rs.getString("unit"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rs.getBigDecimal("salePrice").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rs.getBigDecimal("totalQty").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rs.getBigDecimal("stockValue").toPlainString());
		}

		@Cleanup FileOutputStream outputStream = new FileOutputStream(Paths.get(filePath, "stock-report.xlsx").toString());
		workbook.write(outputStream);
	  }
	});
  }

  public void exportSalesInvoice(Voucher voucher, String filePath) {
	// header
	String sqlVoucher = "select id voucherId, voucherDate, personId, personName, note, rate, rateType, debtPrincipal, debtInterest, debtTotal, lastInterestCalculatedDate as daysOverDue" +
			"from sale_vouchers sv inner join sale_debt sd on sv.id = sd.voucherId " +
			"order by voucherDate desc";
	// lines
	String sqlVoucherLine = "select voucherId, itemId, itemName, unit, qty, unitPrice, lineAmount " +
			"from sale_voucher_lines svl inner join sale_vouchers sd on sv.id = sd.voucherId " +
			"order by voucherDate desc";

	wrapListResult(new String[]{sqlVoucher, sqlVoucherLine}, new SqlAction<>() {
	  @Override
	  public void execListResult(List<ResultSet> rss) throws SQLException, IOException {
		ResultSet rsVoucher = rss.getFirst();
		ResultSet rsVoucherLine = rss.getLast();
		@Cleanup Workbook workbook = new XSSFWorkbook();
		Sheet sheetHeader = workbook.createSheet("header");
		Sheet sheetLine = workbook.createSheet("lines");

		createHeader(rsVoucher, sheetHeader);
		createHeader(rsVoucherLine, sheetLine);

		int rowNum = 1;
		while (rsVoucher.next()) {
		  Row row = sheetHeader.createRow(rowNum++);
		  int colNum = 0;
		  Cell cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getInt("voucherId"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getDate("voucherDate"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getInt("personId"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getString("personName"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getString("note"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getBigDecimal("rate").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getString("rateType"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getBigDecimal("debtPrincipal").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getBigDecimal("debtInterest").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucher.getBigDecimal("debtTotal").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(ChronoUnit.DAYS.between(rsVoucher.getDate("debtTotal").toLocalDate(), LocalDate.now()));
		}
		rowNum = 1;
		while (rsVoucherLine.next()) {
		  Row row = sheetLine.createRow(rowNum++);
		  int colNum = 0;
		  Cell cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucherLine.getInt("voucherId"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucherLine.getInt("itemId"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucherLine.getString("itemName"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucherLine.getString("unit"));
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucherLine.getBigDecimal("qty").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucherLine.getBigDecimal("unitPrice").toPlainString());
		  cell = row.createCell(colNum++);
		  cell.setCellValue(rsVoucherLine.getBigDecimal("lineAmount").toPlainString());
		}

		@Cleanup FileOutputStream outputStream = new FileOutputStream(Paths.get(filePath, "sales-invoice-print.xlsx").toString());
		workbook.write(outputStream);
	  }
	});
  }
}
