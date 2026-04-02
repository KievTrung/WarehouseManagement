package com.KievTrung.ui;

import com.KievTrung.core.dao.PersonDao;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.repository.PersonRepositoryI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class PersonInputDiolog extends JDialog {
  private JPanel contentPane;
  private JButton buttonConfirm;
  private JButton buttonCancel;
  private JTextField textFieldName;
  private JTextField textFieldPhone;
  private JTextField textFieldAddress;
  private JTextField textFieldNote;
  private MainForm parent;

  interface Action {
	void execute(Object obj) throws Exception;
  }

  private final PersonRepositoryI personRepositoryI = new PersonDao();
  private Object obj;
  private Person.Type type;
  private final MainForm.Mode mode;

  public PersonInputDiolog(MainForm parent, Person.Type type, MainForm.Mode mode) {
	super(parent, true);
	this.parent = parent;
	this.type = type;
	this.mode = mode;
	setUp();
  }

  public PersonInputDiolog(MainForm parent, MainForm.Mode mode, Object obj) {
	super(parent, true);
	this.parent = parent;
	this.obj = obj;
	this.mode = mode;
	setUp();
  }

  private void setUp() {
	this.setContentPane(contentPane);
	setLocationRelativeTo(null);
	getRootPane().setDefaultButton(buttonConfirm);
	pack();

	buttonConfirm.addActionListener(e -> confirm());
	buttonCancel.addActionListener(e -> dispose());

	setTitle(String.format("Tạo %s", type == Person.Type.CUSTOMER ? "khách hàng" : "nhà cung cấp"));

	if (mode == MainForm.Mode.MODIFY) {
	  Person person = (Person) obj;
	  textFieldName.setText(person.getName());
	  textFieldPhone.setText(person.getPhone());
	  textFieldAddress.setText(person.getAddress());
	  textFieldNote.setText(person.getNote());
	}
	setVisible(true);
	setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }

  public void confirm() {
	if (mode == MainForm.Mode.CREATE)
	  process(obj -> personRepositoryI.create(type, (Person) obj));
	else {
	  process(c -> {
		((Person) c).setId(((Person) obj).getId());
		personRepositoryI.update(type, (Person) c);
	  });
	}
	dispose();
  }

  private void process(Action action) {
	if (MainForm.showYesNoDialog() == 0) {
	  Person person = new Person();
	  person.setName(textFieldName.getText());
	  person.setPhone(textFieldPhone.getText());
	  person.setAddress(textFieldAddress.getText());
	  person.setNote(textFieldNote.getText());
	  person.setType(type);

	  try {
		action.execute(person);
	  } catch (Exception ex) {
		log.error(ex.getMessage(), ex);
		parent.showErrorDialog(ex.getMessage());
	  }
	}
  }
}

