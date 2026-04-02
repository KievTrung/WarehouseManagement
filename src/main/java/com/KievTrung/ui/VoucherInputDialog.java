package com.KievTrung.ui;

import com.KievTrung.core.domain.*;
import com.KievTrung.core.service.VoucherService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.LocalDateModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Slf4j
public class VoucherInputDialog extends JDialog {
  private JPanel contentPane;
  private JButton payNowButton;
  private JButton buttonCancel;
  private JTextField textFieldNote;
  private JComboBox<String> comboBoxSupplier;
  private JButton buttonInterest;
  private JTable tableItem;
  private JButton addButton;
  private JButton deleteButton;
  private JLabel labelInterest;
  private JButton modifyButton;
  private JPanel datePanel;
  private JButton payLaterButton;
  private JLabel labelTotal;
  private JRadioButton radioButtonMonthly;
  private JRadioButton radioButtonAnnual;
  private JSpinner spinnerInterest;
  private LocalDateModel dateModel;
  @Getter
  private final MainForm parent;

  private final VoucherService voucherService;

  public Voucher voucher;
  private final Map<String, Item> items = new HashMap<>();
  private final Map<String, Person> persons = new HashMap<>();
  private final MainForm.Mode mode;
  private Person.Type type;

  private final String[] col = {"Mã mặt hàng", "Tên mặt hàng", "Đơn vị", "Số lượng", "Giá trị trên 1 đơn vị", "Tổng giá trị"};

  public VoucherInputDialog(MainForm parent, List<Person> persons, List<Item> items, VoucherService voucherService, MainForm.Mode mode) {
	super(parent, true);
	this.parent = parent;
	this.voucherService = voucherService;
	this.mode = mode;
	setUp(persons, items);
  }

  public VoucherInputDialog(MainForm parent, List<Person> persons, List<Item> items, VoucherService voucherService, MainForm.Mode mode, Integer voucherId) {
	super(parent, true);
	this.parent = parent;
	this.voucherService = voucherService;
	this.mode = mode;
	setUp(persons, items);
  }

  private void setUp(List<Person> persons, List<Item> items) {
	setContentPane(contentPane);
	pack();
	// title
	setTitle(String.format("Tạo phiếu %s", type == Person.Type.CUSTOMER ? "xuất" : "nhập"));

	voucher = new Voucher();
	type = persons.getFirst().getType() == Person.Type.CUSTOMER ? Person.Type.CUSTOMER : Person.Type.SUPPLIER;

	payNowButton.addActionListener(e -> onPayNow());
	payLaterButton.addActionListener(e -> onPayLater());
	buttonCancel.addActionListener(e -> onCancel());
	addButton.addActionListener(e -> addItem());
	deleteButton.addActionListener(e -> deleteItem());
	modifyButton.addActionListener(e -> modifyItem());

	items.forEach(item -> this.items.put(item.getName(), item));

	// set combobox
	persons.forEach(p -> {
	  this.persons.put(p.getName(), p);
	  comboBoxSupplier.addItem(p.getName());
	});
	comboBoxSupplier.setSelectedIndex(-1);
	// set table
	MainForm.loadTable(tableItem, col, null);
	// set jdatepicker
	dateModel = new LocalDateModel();
	dateModel.setValue(LocalDate.now());
	dateModel.setSelected(true);
	JDatePicker jDatePicker = new JDatePicker(dateModel);
	datePanel.add(jDatePicker, BorderLayout.CENTER);
	// set spinner Interest
	spinnerInterest.setModel(new SpinnerNumberModel(0., 0., null, 0.01));
	MainForm.setQtyOrInterestInputFormat(spinnerInterest);
	// set radio button
	setVisible(true);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  private void calculateTotalValue() {
	BigDecimal totalValue = BigDecimal.ZERO;
	for (int i = 0; i < tableItem.getRowCount(); i++) {
	  BigDecimal lineAmount = new BigDecimal(String.valueOf(tableItem.getValueAt(i, col.length - 1)));
	  totalValue = totalValue.add(lineAmount);
	}
	labelTotal.setText(totalValue + " VND");
  }

  private void addItem() {
	new ItemInputDiolog(this, items, type);
	String itemName = ItemInputDiolog.voucherLine.getItemName();
	if (itemName != null) {
	  ((DefaultTableModel) tableItem.getModel()).addRow(new Object[]{
			  ItemInputDiolog.voucherLine.getItemId(),
			  itemName,
			  ItemInputDiolog.voucherLine.getUnit(),
			  ItemInputDiolog.voucherLine.getQty(),
			  ItemInputDiolog.voucherLine.getUnitCost(),
			  ItemInputDiolog.voucherLine.getLineAmount(),
	  });
	  ItemInputDiolog.voucherLine = new VoucherLine();
	}
	calculateTotalValue();
  }

  private void modifyItem() {
	int idx = tableItem.getSelectedRow();
	if (idx == -1) {
	  parent.showErrorDialog("No row selected");
	  return;
	}

	ItemInputDiolog.voucherLine.setItemName(String.valueOf(tableItem.getValueAt(idx, 1)));
	ItemInputDiolog.voucherLine.setUnit(String.valueOf(tableItem.getValueAt(idx, 2)));
	ItemInputDiolog.voucherLine.setQty(new BigDecimal(String.valueOf(tableItem.getValueAt(idx, 3))));
	ItemInputDiolog.voucherLine.setUnitCost(new BigDecimal(String.valueOf(tableItem.getValueAt(idx, 4))));
	ItemInputDiolog.voucherLine.setLineAmount(new BigDecimal(String.valueOf(tableItem.getValueAt(idx, 5))));

	ItemInputDiolog itemInputDiolog = new ItemInputDiolog(this, items, type);

	tableItem.setValueAt(ItemInputDiolog.voucherLine.getItemName(), idx, 1);
	tableItem.setValueAt(ItemInputDiolog.voucherLine.getUnit(), idx, 2);
	tableItem.setValueAt(ItemInputDiolog.voucherLine.getQty(), idx, 3);
	tableItem.setValueAt(ItemInputDiolog.voucherLine.getUnitCost(), idx, 4);
	tableItem.setValueAt(ItemInputDiolog.voucherLine.getLineAmount(), idx, 5);

	ItemInputDiolog.voucherLine = new VoucherLine();
	calculateTotalValue();
  }

  private void deleteItem() {
	int idx = tableItem.getSelectedRow();
	if (idx == -1) {
	  parent.showErrorDialog("No row selected");
	  return;
	}
	if (MainForm.showYesNoDialog() == 0) {
	  ((DefaultTableModel) tableItem.getModel()).removeRow(idx);
	  calculateTotalValue();
	}
  }

  private void addVoucher() {
	String name = String.valueOf(comboBoxSupplier.getSelectedItem());
	BigDecimal rate = new BigDecimal(String.valueOf(spinnerInterest.getValue())).setScale(2, RoundingMode.HALF_UP);
	voucher.setPersonId(persons.get(name).getId());
	voucher.setPersonName(name);
	voucher.setVoucherDate(dateModel.getValue());
	voucher.setNote(textFieldNote.getText().trim());
	voucher.setRate(rate);
	if (rate.compareTo(BigDecimal.ZERO) > 0)
	  voucher.setRateType(radioButtonMonthly.isSelected() ? Voucher.Type.MONTHLY : Voucher.Type.ANNUALY);

	Set<VoucherLine> voucherLines = new HashSet<>();
	for (int i = 0; i < tableItem.getRowCount(); i++) {
	  VoucherLine voucherLine = new VoucherLine();
	  try {
		voucherLine.setItemId(Integer.valueOf(String.valueOf(tableItem.getValueAt(i, 0))));
	  } catch (NumberFormatException e) {
		voucherLine.setItemId(null);
	  }

	  voucherLine.setItemName(String.valueOf(tableItem.getValueAt(i, 1)));
	  voucherLine.setUnit(String.valueOf(tableItem.getValueAt(i, 2)));
	  voucherLine.setQty(new BigDecimal(String.valueOf(tableItem.getValueAt(i, 3))));
	  voucherLine.setUnitCost(new BigDecimal(String.valueOf(tableItem.getValueAt(i, 4))));
	  voucherLine.setLineAmount(new BigDecimal(String.valueOf(tableItem.getValueAt(i, 5))));

	  voucherLines.add(voucherLine);
	}
	voucher.setVoucherLines(voucherLines);
  }

  private void onPayNow() {
	try {
	  addVoucher();
	  voucherService.createAndPayNow(type, voucher);
	  dispose();
	} catch (Exception e) {
	  log.error("VoucherInputDialog: ", e);
	  parent.showErrorDialog(e.getMessage());
	}
  }

  private void onPayLater() {
	try {
	  addVoucher();
	  voucherService.create(type, voucher);
	  dispose();
	} catch (Exception e) {
	  log.error("VoucherInputDialog: ", e);
	  parent.showErrorDialog(e.getMessage());
	}
  }

  private void onCancel() {
	dispose();
  }
}
