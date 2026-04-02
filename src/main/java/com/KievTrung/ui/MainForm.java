package com.KievTrung.ui;

import com.KievTrung.core.dao.DebtDao;
import com.KievTrung.core.dao.ItemDao;
import com.KievTrung.core.dao.PaymentDetailDao;
import com.KievTrung.core.dao.PersonDao;
import com.KievTrung.core.domain.*;
import com.KievTrung.core.repository.RepositoryI;
import com.KievTrung.core.repository.PersonRepositoryI;
import com.KievTrung.core.service.ExcelImportService;
import com.KievTrung.core.service.PaymentService;
import com.KievTrung.core.service.VoucherService;
import com.KievTrung.core.service.ServiceI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

@Slf4j
public class MainForm extends JFrame {
  public JPanel main;
  private JPanel navigation;
  private JTable tableCustomer;
  private JButton createButton;
  private JButton modifyButton;
  private JButton deleteButton;
  private JButton findButton;
  private JTabbedPane tabbedPaneContent;
  private JTable tableSupplier;
  private JTable tableItem;
  private JTabbedPane tabbedPanePurchase;
  private JTabbedPane tabbedPaneSale;
  private JTable tablePurchaseVoucher;
  private JTable tablePayment;
  private JTable tableSupplierDebt;
  private JTable tableSaleVoucher;
  private JTable tableCustomerDebt;
  private JTable tableCollection;
  private JTable tablePaymentDetail;
  private JTable tablePurchaseVoucherLine;
  private JTable tableSaleVoucherLine;
  private JButton importButton;
  private JPanel content;

//    @Getter
//    public enum Tab {
//        CUSTOMER(0), SUPPLIER(1), ITEM(2), PURCHASE(3), SALE(4), REPORT(5);
//        final int tabIdx;
//        Tab(int tabIdx){
//            this.tabIdx = tabIdx;
//        }
//    }
//
//    @Getter
//    public enum VoucherSubTab {
//        VOUCHER(0), DEBT(1), PAYMENT(2);
//        final int tabIdx;
//        VoucherSubTab(int tabIdx){
//            this.tabIdx = tabIdx;
//        }
//    }

  public enum Mode {CREATE, MODIFY}

  private final String[] personCol = {"Mã cá nhân", "Tên", "Số điện thoại", "Địa chỉ", "Ghi chú", "Ngày tạo", "Ngày cập nhật"};
  private final String[] itemCol = {"Mã mặt hàng", "Tên mặt hàng", "Số lượng", "Giá bán(VND)", "Đơn vị", "Ghi chú",  "Ngày nhập hàng gần nhất", "Ngày tạo", "Ngày cập nhật"};
  private final String[] voucherCol = {"Mã phiếu", "Ngày tạo phiếu", "Mã cá nhân", "Tên cá nhân", "Lãi xuất(%)", "Loại lãi xuất", "Ghi chú", "Ngày tạo", "Ngày cập nhật"};
  private final String[] voucherLineCol = {"Mã line phiếu", "Mã mặt hàng", "Tên mặt hàng", "Đơn vị", "Số lượng", "Trị giá(VND)", "Tổng trị giá(VND)", "Ngày tạo", "Ngày cung cấp"};
  private final String[] paymentCol = {"Mã thanh toán", "Ngày thanh toán", "Mã cá nhân", "Tên cá nhân", "Số tiền thanh toán(VND)", "Ghi chú", "Ngày cập nhật"};
  private final String[] paymentDetailCol = {"Mã phiếu", "Ngày tạo", "Nợ gốc ban đầu(VND)", "Nợ lãi ban đầu(VND)", "Nợ tổng ban đầu(VND)", "Lãi xuất đã thanh toán(VND)", "Gốc đã thanh toán(VND)", "Nợ gốc còn lại(VND)", "Nợ lãi còn lại(VND)"};
  private final String[] debtCol = {"Mã phiếu", "Mã cá nhân", "Trạng thái thanh toán toàn bộ","Nợ gốc(VND)", "Nợ lãi(VND)", "Nợ tổng(VND)", "Ngày tạo", "Ngày cập nhật lãi gần nhất"};

  private final RepositoryI<Item> itemRepository = new ItemDao();
  private final RepositoryI<PaymentDetail> paymentDetailRepository = new PaymentDetailDao();
  private final PersonRepositoryI personRepository = new PersonDao();
  private final RepositoryI<Debt> debtRepository = new DebtDao();

  private final ExcelImportService excelImportService = new ExcelImportService();
  private final VoucherService voucherService = new VoucherService();
  private final ServiceI<Payment> paymentService = new PaymentService();

  private Handler<?> handler;
  private final ItemHandler itemHandler = new ItemHandler();
  private final ReportHandler reportHandler = new ReportHandler();
  private final PersonHandler supplierHandler = new PersonHandler(Person.Type.SUPPLIER);
  private final PersonHandler customerHandler = new PersonHandler(Person.Type.CUSTOMER);
  private final PaymentHandler customerPaymentHandler = new PaymentHandler(Person.Type.CUSTOMER);
  private final PaymentHandler supplierPaymentHandler = new PaymentHandler(Person.Type.SUPPLIER);
  private final VoucherHandler customerVoucherHandler = new VoucherHandler(Person.Type.CUSTOMER);
  private final VoucherHandler supplierVoucherHandler = new VoucherHandler(Person.Type.SUPPLIER);
  private final DebtHandler customerDebtHandler = new DebtHandler(Person.Type.CUSTOMER);
  private final DebtHandler supplierDebtHandler = new DebtHandler(Person.Type.SUPPLIER);

  public MainForm() {

	setVisible(true);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setContentPane(main);
	pack();

	setTitle("Quản lí kho");

	tabbedPaneContent.addChangeListener(_ -> switchHandler());
	tabbedPanePurchase.addChangeListener(_ -> switchVoucherHandler(Person.Type.SUPPLIER));
	tabbedPaneSale.addChangeListener(_ -> switchVoucherHandler(Person.Type.CUSTOMER));

	createButton.addActionListener(_ -> handler.create());
	deleteButton.addActionListener(_ -> handler.delete());
	modifyButton.addActionListener(_ -> handler.modify());
	findButton.addActionListener(_ -> handler.find());
	importButton.addActionListener(_ -> handler.import_());

	switchHandler();
  }

  private void switchHandler() {
	switch (tabbedPaneContent.getSelectedIndex()) {
	  case 1 -> {
		handler = supplierHandler;
	  }
	  case 2 -> {
		handler = itemHandler;
	  }
	  case 3 -> {
		switchVoucherHandler(Person.Type.SUPPLIER);
		return;
	  }
	  case 4 -> {
		switchVoucherHandler(Person.Type.CUSTOMER);
		return;
	  }
	  case 5 -> {
		handler = reportHandler;
	  }
	  default -> {
		handler = customerHandler;
	  }
	}
	handler.getAll();
  }

  private void switchVoucherHandler(Person.Type type) {
	switch (tabbedPanePurchase.getSelectedIndex()) {
	  case 1 -> {
		handler = type == Person.Type.CUSTOMER ? customerDebtHandler : supplierDebtHandler;
	  }
	  case 2 -> {
		handler = type == Person.Type.CUSTOMER ? customerPaymentHandler : supplierPaymentHandler;
	  }
	  default -> {
		handler = type == Person.Type.CUSTOMER ? customerVoucherHandler : supplierVoucherHandler;
	  }
	}
	handler.getAll();
  }

  public static void loadTable(JTable table, String[] col, List<Object[]> data) {
	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	var tableModel = new DefaultTableModel() {
	  @Override
	  public boolean isCellEditable(int row, int column) {
		return false;
	  }
	};
	// add header
	Arrays.stream(col).forEach(tableModel::addColumn);
	// load data
	if (data != null)
	  data.forEach(tableModel::addRow);
	table.setModel(tableModel);
  }

  public static Integer showYesNoDialog() {
	return JOptionPane.showConfirmDialog(null, "Do you confirm?", "Notification", JOptionPane.YES_NO_OPTION);
  }

  public void showErrorDialog(String msg) {
	JOptionPane.showMessageDialog(this, msg.substring(msg.indexOf(":") + 1), "Error", JOptionPane.ERROR_MESSAGE);
  }

  public static void showInformation(String msg, String title) {
	JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
  }

  public static String showInput(String msg, String title) {
	return JOptionPane.showInputDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
  }

  public static void setMoneyInputFormat(JSpinner spinner) {
	JFormattedTextField textFieldCost = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
	// only allow to accept float
	NumberFormatter formatterCost = new NumberFormatter(new DecimalFormat());
	formatterCost.setMinimum(0);
	formatterCost.setValueClass(Integer.class);
	formatterCost.setAllowsInvalid(false);
	textFieldCost.setFormatterFactory(
			new DefaultFormatterFactory(formatterCost)
	);
  }

  public static void setQtyOrInterestInputFormat(JSpinner spinner) {
	JFormattedTextField textFieldQty = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
	NumberFormatter formatterQty = new NumberFormatter(new DecimalFormat("#.00"));
	formatterQty.setMinimum(0.01);
	formatterQty.setValueClass(Double.class);
	formatterQty.setAllowsInvalid(false);
	textFieldQty.setFormatterFactory(
			new DefaultFormatterFactory(formatterQty)
	);
  }

  interface Handler<T> {
	List<Object[]> prepareTableRow(List<T> o);

	void getAll();

	void create();

	void delete();

	void modify();

	void find();

	void import_();
  }

  class PersonHandler implements Handler<Person> {
	Person.Type type;
	JTable personTable;

	public PersonHandler(Person.Type type) {
	  this.type = type;
	  this.personTable = type == Person.Type.CUSTOMER ? tableCustomer : tableSupplier;
	}

	@Override
	public List<Object[]> prepareTableRow(List<Person> persons) {
	  List<Object[]> objects = new ArrayList<>();
	  persons.forEach(c -> objects.add(new Object[]{
			  c.getId(),
			  c.getName(),
			  c.getPhone(),
			  c.getAddress(),
			  c.getNote(),
			  c.getCreateDate(),
			  c.getUpdateDate()
	  }));
	  return objects;
	}

	@Override
	public void getAll() {
	  loadTable(personTable, personCol, prepareTableRow(personRepository.findAll(type)));
	}

	@Override
	public void create() {
	  new PersonInputDiolog(MainForm.this, type, Mode.CREATE);
	  getAll();
	}

	@Override
	public void delete() {
	  int idx = personTable.getSelectedRow();
	  if (idx == -1) {
		showErrorDialog("No row selected");
		return;
	  }
	  if (showYesNoDialog() == 0) {
		Integer id = (Integer) personTable.getValueAt(idx, 0);
		try {
		  personRepository.delete(type, id);
		  getAll();
		} catch (Exception e) {
		  log.error(e.getMessage(), e);
		  showErrorDialog(e.getMessage());
		}
	  }
	}

	@Override
	public void modify() {
	  int idx = personTable.getSelectedRow();
	  if (idx == -1) {
		showErrorDialog("No row selected");
		return;
	  }
	  Person person = new Person();
	  person.setId((Integer) personTable.getValueAt(idx, 0));
	  person.setName((String) personTable.getValueAt(idx, 1));
	  person.setPhone((String) personTable.getValueAt(idx, 2));
	  person.setAddress((String) personTable.getValueAt(idx, 3));
	  person.setNote((String) personTable.getValueAt(idx, 4));
	  person.setType(Person.Type.CUSTOMER);

	  PersonInputDiolog input = new PersonInputDiolog(MainForm.this, Mode.MODIFY, person);
	  getAll();
	}

	@Override
	public void find() {
	  String name = JOptionPane.showInputDialog(null, "Tab name: ", "Find", JOptionPane.INFORMATION_MESSAGE);
	  if (name == null || name.isBlank()) return;
	  loadTable(personTable, personCol, prepareTableRow(personRepository.findByName(type, name)));
	}

	@Override
	public void import_() {
	  String filePath = showInput("Nhập đường dẫn: ", String.format("Tạo %s", type == Person.Type.CUSTOMER ? "khách hàng" : "nhà cung cấp"));
	  if (filePath == null || filePath.isEmpty()) return;
	  try{
	  	List<Person> persons = excelImportService.importPersons(type, filePath);
		personRepository.create(type, persons);
		getAll();
	  }
	  catch (Exception e){
		log.error("Import {}", type, e);
		showErrorDialog(e.getMessage());
	  }
	}
  }

  class ItemHandler implements Handler<Item> {

	public ItemHandler() {
	}

	@Override
	public List<Object[]> prepareTableRow(List<Item> o) {
	  return List.of();
	}

	@Override
	public void getAll() {
	  List<Item> items = itemRepository.findAll(null);
	  List<Object[]> objects = new ArrayList<>();
	  items.forEach(c -> objects.add(new Object[]{
			  c.getId(),
			  c.getName(),
			  c.getTotalQty(),
			  c.getSalePrice(),
			  c.getUnit(),
			  c.getNote(),
			  c.getLastUpdateStock(),
			  c.getCreateDate(),
			  c.getUpdateDate()
	  }));
	  loadTable(tableItem, itemCol, objects);
	}

	@Override
	public void create() {

	}

	@Override
	public void delete() {

	}

	@Override
	public void modify() {

	}

	@Override
	public void find() {

	}

	@Override
	public void import_() {
	  String filePath = showInput("Nhập đường dẫn: ", "Tạo mặt hàng");
	  if (filePath == null || filePath.isEmpty()) return;
	  try{
		List<Item> items = excelImportService.importItems(filePath);
		itemRepository.create(null, items);
		getAll();
	  }
	  catch (Exception e){
		log.error("Import item: ", e);
		showErrorDialog(e.getMessage());
	  }
	}
  }

  @Getter
  class VoucherHandler implements Handler<Voucher> {
	private final List<Person> persons;
	private final List<Item> items;
	private final Person.Type type;

	public VoucherHandler(Person.Type type) {
	  persons = personRepository.findAll(type);
	  items = itemRepository.findAll(null);
	  this.type = type;
	  boolean isCustomer = type == Person.Type.CUSTOMER;

	  JTable tableVoucher = isCustomer ? tableSaleVoucher : tablePurchaseVoucher;
	  JTable tableVoucherLine = isCustomer ? tableSaleVoucherLine : tablePurchaseVoucherLine;
	  tableVoucher.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		  int row = tableVoucher.rowAtPoint(e.getPoint());
		  int col = tableVoucher.columnAtPoint(e.getPoint());
		  if (row >= 0) {
			// Convert to model index in case columns are reordered
			int modelRow = tableVoucher.convertRowIndexToModel(row);
			Object value = tableVoucher.getModel().getValueAt(modelRow, 0);
			Integer voucherId = Integer.valueOf(value.toString());
			// table voucher line
			loadTable(tableVoucherLine, voucherLineCol, prepareDetailTableRow(voucherService.findAllLineById(type, voucherId)));

		  }
		}
	  });
	}

	@Override
	public List<Object[]> prepareTableRow(List<Voucher> vouchers) {
	  List<Object[]> objects = new ArrayList<>();
	  vouchers.forEach(c -> objects.add(new Object[]{
			  c.getId(),
			  c.getVoucherDate(),
			  c.getPersonId(),
			  c.getPersonName(),
			  c.getRate(),
			  c.getRateType() == null ? "n/a" : c.getRateType(),
			  c.getNote(),
			  c.getCreateDate(),
			  c.getUpdateDate()
	  }));
	  return objects;
	}

	public List<Object[]> prepareDetailTableRow(List<VoucherLine> l) {
		List<Object[]> objects = new ArrayList<>();
		l.forEach(o -> objects.add(new Object[]{
				o.getId(),
				o.getItemId(),
				o.getItemName(),
				o.getUnit(),
				o.getQty(),
				o.getUnitCost(),
				o.getLineAmount(),
				o.getCreateDate(),
				o.getUpdateDate()
		}));
		return objects;
	}

	@Override
	public void getAll() {
	  List<Voucher> vouchers = voucherService.getAll(type);
	  loadTable(type == Person.Type.CUSTOMER ? tableSaleVoucher : tablePurchaseVoucher, voucherCol, prepareTableRow(vouchers));
	}

	@Override
	public void create() {
	  if (persons.isEmpty()) {
		showErrorDialog(String.format("Chưa có %s được tạo", type == Person.Type.CUSTOMER ? "khách hàng" : "nhà cung cấp"));
		return;
	  }
	  new VoucherInputDialog(MainForm.this, persons, items, voucherService, Mode.CREATE);
	  getAll();
	}

	@Override
	public void delete() {

	}

	@Override
	public void modify() {
	  int idx = tablePurchaseVoucher.getSelectedRow();
	  if (idx == -1) {
		showErrorDialog("No row selected");
		return;
	  }
	  VoucherInputDialog voucherInputDialog = new VoucherInputDialog(
			  MainForm.this, persons, items, voucherService, Mode.MODIFY, Integer.valueOf((String) tablePurchaseVoucher.getValueAt(idx, 0))
	  );
	  getAll();
	}

	@Override
	public void find() {
	  findButton.setVisible(false);
	}

	@Override
	public void import_() {
	  String filePath = showInput("Nhập đường dẫn: ", String.format("Tạo phiếu %s", type == Person.Type.CUSTOMER ? "bán" : "mua"));
	  if (filePath == null || filePath.isEmpty()) return;
	  try{
		List<Voucher> vouchers = excelImportService.importVouchers(type, filePath);
		voucherService.create(type, vouchers);
		getAll();
	  }
	  catch (Exception e){
		log.error("Import voucher: ", e);
		showErrorDialog(e.getMessage());
	  }
	}
  }

  class PaymentHandler implements Handler<Payment> {
	private final Person.Type type;

	public PaymentHandler(Person.Type type) {
	  this.type = type;
	  // set up table clicking listener
//            JTable table = type == Person.Type.CUSTOMER ? tableP : tablePurchaseVoucher;
	  tablePayment.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		  int row = tablePayment.rowAtPoint(e.getPoint());
		  if (row >= 0) {
			// Convert to model index in case columns are reordered
			int modelRow = tablePayment.convertRowIndexToModel(row);
			Object value = tablePayment.getModel().getValueAt(modelRow, 0);
			Integer paymentId = Integer.valueOf(value.toString());
			loadTable(tablePaymentDetail, paymentDetailCol, prepareDetailTableRow(paymentDetailRepository.findAllById(type, paymentId)));
		  }
		}
	  });
	}

	@Override
	public List<Object[]> prepareTableRow(List<Payment> l) {
	  List<Object[]> objects = new ArrayList<>();
	  l.forEach(o -> objects.add(new Object[]{
			  o.getId(),
			  o.getPaymentDate(),
			  o.getPersonId(),
			  o.getPersonName(),
			  o.getPaidAmount(),
			  o.getNote(),
//                    FormatDateTime.toStr(o.getCreateDate()),
			  o.getUpdateDate()
	  }));
	  return objects;
	}

	public List<Object[]> prepareDetailTableRow(List<PaymentDetail> l) {
	  List<Object[]> objects = new ArrayList<>();
	  l.forEach(o -> objects.add(new Object[]{
			  o.getId(),
			  o.getCreateDate(),
			  o.getDebtBeforePrincipal(),
			  o.getDebtBeforeInterest(),
			  o.getDebtBeforeTotal(),
			  o.getPaidInterest(),
			  o.getPaidPrincipal(),
			  o.getRemainingPrincipal(),
			  o.getRemainingInterest(),
	  }));
	  return objects;
	}

	@Override
	public void getAll() {
	  loadTable(tablePayment, paymentCol, prepareTableRow(paymentService.getAll(type)));
	}

	@Override
	public void create() {
	  List<Person> persons = personRepository.getAllWithDebt(type);
	  new PaymentInputDiolog(MainForm.this, persons, paymentService);
	  getAll();
	}

	@Override
	public void delete() {

	}

	@Override
	public void modify() {

	}

	@Override
	public void find() {

	}

	@Override
	public void import_() {

	}
  }

  class DebtHandler implements Handler<Debt> {
	private final Person.Type type;

	public DebtHandler(Person.Type type) {
		this.type = type;
	}

	@Override
	public List<Object[]> prepareTableRow(List<Debt> o) {
	  List<Object[]> objects = new ArrayList<>();
	  o.forEach(d -> objects.add(new Object[]{
			  d.getVoucherId(),
			  d.getPersonId(),
			  d.getIsSettled(),
			  d.getDebtPrincipal(),
			  d.getDebtInterest(),
			  d.getDebtTotal(),
			  d.getCreatedDate(),
			  d.getLastInterestCalculatedDate(),
	  }));
	  return objects;
	}

	@Override
	public void getAll() {
		loadTable(type == Person.Type.CUSTOMER ? tableCustomerDebt : tableSupplierDebt, debtCol, prepareTableRow(debtRepository.findAll(type)));
	}

	@Override
	public void create() {

	}

	@Override
	public void delete() {

	}

	@Override
	public void modify() {

	}

	@Override
	public void find() {

	}

	@Override
	public void import_() {

	}
  }

  class ReportHandler implements Handler<Object> {

	@Override
	public List<Object[]> prepareTableRow(List<Object> o) {
	  return List.of();
	}

	@Override
	public void getAll() {

	}

	@Override
	public void create() {

	}

	@Override
	public void delete() {

	}

	@Override
	public void modify() {

	}

	@Override
	public void find() {

	}

	@Override
	public void import_() {

	}
  }
}
