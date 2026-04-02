package com.KievTrung.core.service;

import com.KievTrung.core.domain.*;
import com.KievTrung.util.exception.IdInvalidException;
import com.KievTrung.util.helper.DaoHelper;
import com.KievTrung.util.helper.Validation;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class ExcelImportService extends DaoHelper<Object> {
//  For each import:
//		  - [ ] Validate required columns exist
//  - [ ] Validate data types & ranges
//  - [ ] Check FK constraints (mã khách, mã hàng exist)
//  - [ ] Recalculate lineAmount, debtTotal
//  - [ ] Transaction: import all or rollback
//  - [ ] Return detailed error report if validation fails

  // personCol = {"Person id", "Person name", "Phone", "Address", "Note"};
  // voucherCol = {"Voucher id", "Person id", "Person name", "Rate", "Rate type", "Note", "Create", "Update"};

  private void validation(Object obj, String filePath, Integer rowId){
	try {
	  Validation.validate(obj);
	} catch (RuntimeException e) {
	  log.error("File: {}, Row: {}", filePath, rowId, e);
	  throw new RuntimeException(e);
	}
  }

  private String readCell(Cell cell){
	return (new DataFormatter()).formatCellValue(cell).trim();
  }

  private LocalDate readDateCell(Cell cell){
	return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public List<Person> importPersons(Person.Type type, String filePath) throws IOException, IdInvalidException {
	@Cleanup FileInputStream inputStream = new FileInputStream(filePath);
	@Cleanup Workbook workBook = new XSSFWorkbook(inputStream);
	Sheet sheet = workBook.getSheet("Persons");

	int rowNum = sheet.getPhysicalNumberOfRows();
	List<Person> persons = new ArrayList<>();

	for (int rowId = 1; rowId < rowNum; rowId++){
	  Row row = sheet.getRow(rowId);
	  Person person;
	  try{
		person = new Person(
			  Integer.parseInt(readCell(row.getCell(0))),
			  readCell(row.getCell(1)),
			  readCell(row.getCell(2)),
			  readCell(row.getCell(3)),
			  readCell(row.getCell(4)),
			  LocalDateTime.now(),
			  LocalDateTime.now(),
			  type
		);
	  }catch(Exception e){
		throw new IdInvalidException("Invalid person id detected");
	  }
	  validation(person, filePath, rowId);
	  log.info("import {}: {}", type, person);
	  persons.add(person);
	}
	return persons;
  }

  public List<Item> importItems(String filePath) throws IOException {
	@Cleanup FileInputStream inputStream = new FileInputStream(filePath);
	@Cleanup Workbook workBook = new XSSFWorkbook(inputStream);
	Sheet sheet = workBook.getSheet("items");

	List<Item> items = new ArrayList<>();

	for(int i = 1; i < sheet.getPhysicalNumberOfRows(); i++){
	  Row row = sheet.getRow(i);
	  Item item;
	  int colId = 0;
	  try {
		item = new Item(
			   Integer.parseInt(readCell(row.getCell(colId++))),
			   readCell(row.getCell(colId++)),
			   readCell(row.getCell(colId++)),
			   new BigDecimal(readCell(row.getCell(colId++))),
				BigDecimal.ZERO,
				readCell(row.getCell(colId++)),
				LocalDateTime.now(),
				LocalDateTime.now(),
				LocalDateTime.now()
		);
		log.info("import item: {}", item);
	  }catch(Exception e){
		log.error("Error: col {}, row {}", i, --colId);
		throw new IdInvalidException(String.format("Invalid cell detected row %d, col %d", i, colId));
	  }
	  validation(item, filePath, i);
	  items.add(item);
	}
	return items;
  }

  public List<Voucher> importVouchers(Person.Type type, String filePath) throws Exception {
	@Cleanup FileInputStream inputStream = new FileInputStream(filePath);
	@Cleanup Workbook workBook = new XSSFWorkbook(inputStream);
	Sheet sheetHeader = workBook.getSheet("headers");
	Sheet sheetLine = workBook.getSheet("lines");

	Map<Integer, Voucher> integerVoucherMap = new HashMap<>();

	// voucher
	for(int i = 1; i < sheetHeader.getPhysicalNumberOfRows(); i++){
	  Row row = sheetHeader.getRow(i);
	  int colId = 0;
	  int voucherId = Integer.parseInt(readCell(row.getCell(colId++)));
	  Voucher voucher;
	  String h = "";
	  try{
		voucher = new Voucher(
				voucherId,
				readDateCell(row.getCell(colId++)),
				Integer.parseInt(readCell(row.getCell(colId++))),
				readCell(row.getCell(colId++)),
				new BigDecimal(readCell(row.getCell(colId++))), // rate
				Voucher.Type.valueOf(h = readCell(row.getCell(colId++)).toUpperCase()), // rateType
				readCell(row.getCell(colId++)),
				LocalDateTime.now(),
				LocalDateTime.now(),
				null,
				type,
				new HashSet<>()
		);
		integerVoucherMap.put(voucherId, voucher);
		log.info("import voucher: {}", voucher);
	  }catch(Exception e){
		String msg = String.format("Invalid cell detected sheet %s row %d, col %d", sheetHeader.getSheetName(), i, colId);
		log.error(msg, e);
		log.debug(h);
		throw new Exception(msg);
	  }
	}

	// voucher line
	for(int i = 1; i < sheetLine.getPhysicalNumberOfRows(); i++){
	  Row row = sheetLine.getRow(i);
	  int rowId = 0;
	  int voucherId = Integer.parseInt(readCell(row.getCell(rowId++)));
	  BigDecimal qty;
	  BigDecimal unitCost;
	  VoucherLine voucherLine;
	  try{
		voucherLine = new VoucherLine(
				null,
				voucherId,
				Integer.parseInt(readCell(row.getCell(rowId++))),
				readCell(row.getCell(rowId++)),
				readCell(row.getCell(rowId++)),
				qty = new BigDecimal(readCell(row.getCell(rowId++))),
				unitCost = new BigDecimal(readCell(row.getCell(rowId++))),
				qty.multiply(unitCost),
				LocalDateTime.now(),
				LocalDateTime.now()
		);
		validation(voucherLine, filePath, i);
		integerVoucherMap.get(voucherId).getVoucherLines().add(voucherLine);
		log.info("import voucher line: {}", voucherLine);
	  }catch(Exception e){
		String msg = String.format("Invalid cell detected sheet %s col %d, row %d", sheetHeader.getSheetName(), i, rowId);
		log.error(msg, e);
		throw new Exception(msg);
	  }
	}

	return new ArrayList<>(integerVoucherMap.values());
  }

  public List<Payment> importPayment(Person.Type type, String filePath) throws IOException {
	@Cleanup FileInputStream inputStream = new FileInputStream(filePath);
	@Cleanup Workbook workBook = new XSSFWorkbook(inputStream);
	Sheet sheet = workBook.getSheet("Payments");

	int rowNum = sheet.getPhysicalNumberOfRows();
	List<Payment> payments = new ArrayList<>();

	for (int rowId = 1; rowId < rowNum; rowId++){
	  Row row = sheet.getRow(rowId);

	  Payment payment = new Payment(
			  Double.valueOf(row.getCell(0).getNumericCellValue()).intValue(),
			  row.getCell(1).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
			  Double.valueOf(row.getCell(2).getNumericCellValue()).intValue(),
			  row.getCell(3).getStringCellValue(),
			  new BigDecimal(row.getCell(4).getNumericCellValue()),
			  row.getCell(5).getStringCellValue(),
			  LocalDateTime.now(),
			  LocalDateTime.now(),
			  null
	  );
	  validation(payment, filePath, rowId);
	  payments.add(payment);
	}
	return payments;
  }
}
