package com.KievTrung.ui;

import com.KievTrung.core.domain.Item;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.domain.VoucherLine;
import com.KievTrung.util.helper.Validation;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
public class ItemInputDiolog extends JDialog {
  private JPanel contentPane;
  private JButton buttonConfirm;
  private JButton buttonCancel;
  private JComboBox<String> comboBoxItem;
  private JSpinner spinnerCost;
  private JTextField textFieldUnit;
  private JSpinner spinnerQuantity;
  private JLabel labelTotal;
  private JButton totalButton;
  private JLabel labelCostPrice;
  private VoucherInputDialog parent;

  public static VoucherLine voucherLine = new VoucherLine();
  private final Map<String, Item> items;

  public ItemInputDiolog(VoucherInputDialog parent, Map<String, Item> items, Person.Type type) {
	super(parent, true);
	this.items = items;
	setContentPane(contentPane);
	setLocationRelativeTo(null);
	getRootPane().setDefaultButton(buttonConfirm);
	pack();
	// title
	setTitle(String.format("%s mặt hàng", type == Person.Type.CUSTOMER ? "Nhập" : "Xuất"));
	// set item combobox
	String name = voucherLine.getItemName();
	if (name != null) {
	  items.remove(voucherLine.getItemName());
	  comboBoxItem.addItem(name);

	  textFieldUnit.setText(voucherLine.getUnit());
	  spinnerCost.setValue(voucherLine.getUnitCost());
	  spinnerQuantity.setValue(voucherLine.getQty());
	  labelTotal.setText(voucherLine.getLineAmount().toPlainString() + " VND");
	}
	comboBoxItem.setSelectedIndex(-1);
	items.keySet().forEach(itemName -> comboBoxItem.addItem(itemName));
	comboBoxItem.setEditable(true);

	spinnerQuantity.setModel(new SpinnerNumberModel(1., 0.01, null, 1.));

	if (type == Person.Type.CUSTOMER) {
	  comboBoxItem.addActionListener(e -> {
		String selectedItem = (String) comboBoxItem.getSelectedItem();
		Item item = items.get(selectedItem);
		BigDecimal qty = item.getTotalQty();
		if (qty.compareTo(new BigDecimal("0")) == 0) {
		  parent.getParent().showErrorDialog("Out of stock");
		  return;
		}
		textFieldUnit.setText(item.getUnit());
		spinnerCost.setValue(item.getSalePrice());
		spinnerQuantity.setModel(new SpinnerNumberModel(1., 0.01, qty, 1.));
	  });
	  comboBoxItem.setEditable(false);
	  textFieldUnit.setEnabled(false);
	  spinnerCost.setEnabled(false);
	}

	spinnerCost.setModel(new SpinnerNumberModel(0, 0, null, 1000));

	// set number format for unit cost and qty
	MainForm.setMoneyInputFormat(spinnerCost);
	MainForm.setQtyOrInterestInputFormat(spinnerQuantity);

	totalButton.addActionListener(e -> calculateTotalAmount());
	buttonConfirm.addActionListener(e -> onOK());
	buttonCancel.addActionListener(e -> onCancel());

	setVisible(true);
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
  }

  private void calculateTotalAmount() {
	BigDecimal qty = new BigDecimal(String.valueOf(spinnerQuantity.getValue())).setScale(2, RoundingMode.HALF_UP);
	BigDecimal unitCost = new BigDecimal(String.valueOf(spinnerCost.getValue()));
	BigDecimal lineAmount = qty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
	labelTotal.setText(lineAmount.toPlainString() + " VND");
  }

  private void onOK() {
	if (MainForm.showYesNoDialog() == 0) {
	  String selectedItem = (String) comboBoxItem.getSelectedItem();
	  voucherLine.setItemName(selectedItem);
	  voucherLine.setItemId(items.get(selectedItem) == null ? null : items.get(selectedItem).getId());
	  voucherLine.setUnit(textFieldUnit.getText());
	  BigDecimal qty = new BigDecimal(String.valueOf(spinnerQuantity.getValue())).setScale(2, RoundingMode.HALF_UP);
	  voucherLine.setQty(qty);
	  BigDecimal unitCost = new BigDecimal(String.valueOf(spinnerCost.getValue()));
	  voucherLine.setUnitCost(unitCost);
	  BigDecimal lineAmount = qty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
	  voucherLine.setLineAmount(lineAmount);

	  try {
		Validation.validate(voucherLine);
		dispose();
	  } catch (Exception e) {
		log.error("ItemInputDiolog: ", e);
		parent.getParent().showErrorDialog(e.getMessage());
	  }
	}
  }

  private void onCancel() {
	dispose();
  }
}
