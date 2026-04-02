package com.KievTrung.ui;

import com.KievTrung.core.domain.Payment;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.service.ServiceI;
import lombok.extern.slf4j.Slf4j;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.LocalDateModel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PaymentInputDiolog extends JDialog {
  private JPanel contentPane;
  private JButton buttonConfirm;
  private JButton buttonCancel;
  private JComboBox<String> comboBoxPerson;
  private JSpinner spinnerAmount;
  private JTextField textFieldNote;
  private JPanel panelDate;
  private JDatePicker jDatePicker;
  private LocalDateModel dateModel;
  private MainForm parent;

  private final ServiceI<Payment> paymentService;
  private Map<String, Person> personMap = new HashMap<>();

  public PaymentInputDiolog(MainForm frame, List<Person> persons, ServiceI<Payment> paymentService) {
	super(frame, true);
	this.parent = parent;
	this.paymentService = paymentService;

	setContentPane(contentPane);

	// title
	setTitle("Tạo thanh toán");

	buttonConfirm.addActionListener(e -> onOK());
	buttonCancel.addActionListener(e -> onCancel());

	// init date picker
	dateModel = new LocalDateModel();
	jDatePicker = new JDatePicker(dateModel);
	panelDate.add(jDatePicker, BorderLayout.CENTER);

	// load person into combobox
	persons.forEach(p -> {
	  personMap.put(p.getName(), p);
	  comboBoxPerson.addItem(p.getName());
	});
	comboBoxPerson.setSelectedIndex(-1);

	// set format for textfield
	MainForm.setMoneyInputFormat(spinnerAmount);

	setVisible(true);
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
  }

  private void onOK() {
	try {
	  if (MainForm.showYesNoDialog() == 0) {
		Payment payment = new Payment();
		String name = String.valueOf(comboBoxPerson.getSelectedItem());
		payment.setPersonId(personMap.get(name).getId());
		payment.setPersonName(name);
		payment.setPaymentDate(dateModel.getValue());
		payment.setPaidAmount(new BigDecimal(String.valueOf(spinnerAmount.getValue())));
		payment.setNote(textFieldNote.getText());
		// todo
		paymentService.create(null, payment);
		dispose();
	  }
	} catch (Exception e) {
	  log.error("PaymentInputDiolog: ", e);
	  parent.showErrorDialog(e.getMessage());
	}
  }

  private void onCancel() {
	dispose();
  }
}
