-- customers
CREATE TABLE IF NOT EXISTS customers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT UNIQUE NOT NULL,
  phone TEXT,
  address TEXT,
  note TEXT,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- suppliers
CREATE TABLE IF NOT EXISTS suppliers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT UNIQUE NOT NULL,
  phone TEXT,
  address TEXT,
  note TEXT,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- items (hàng hóa)
CREATE TABLE IF NOT EXISTS items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT UNIQUE NOT NULL,
  unit TEXT NOT NULL,
  salePrice DECIMAL(12, 0) NOT NULL,
  totalQty DECIMAL(10, 2) NOT NULL DEFAULT 0,
  note TEXT,
  lastUpdateStock TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- purchase_vouchers (phiếu nhập)
CREATE TABLE IF NOT EXISTS purchase_vouchers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  personId INTEGER NOT NULL,
  personName TEXT,
  voucherDate DATE NOT NULL,
  rateType TEXT NOT NULL, -- 'ANNUAL', 'MONTHLY'
  rate DECIMAL(5, 2) NOT NULL, -- %, e.g., 12.00
  note TEXT,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deletedAt TIMESTAMP NULL,
  FOREIGN KEY (personId) REFERENCES suppliers(id)
);

-- purchase_voucher_lines
CREATE TABLE IF NOT EXISTS purchase_voucher_lines (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  voucherId INTEGER NOT NULL,
  itemId INTEGER NOT NULL,
  itemName TEXT NOT NULL,
  unit TEXT NOT NULL,
  qty DECIMAL(10, 2) NOT NULL,
  unitCost DECIMAL(12, 0) NOT NULL,
  lineAmount DECIMAL(14, 2) NOT NULL,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (voucherId) REFERENCES purchase_vouchers(id) ON DELETE CASCADE,
  FOREIGN KEY (itemId) REFERENCES items(id)
);

-- sales_vouchers (phiếu xuất)
CREATE TABLE IF NOT EXISTS sale_vouchers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  personId INTEGER NOT NULL,
  personName TEXT NOT NULL,
  voucherDate DATE NOT NULL,
  rateType TEXT NOT NULL, -- 'ANNUAL', 'MONTHLY'
  rate DECIMAL(5, 2) NOT NULL, -- %, e.g., 12.00
  note TEXT,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deletedAt TIMESTAMP NULL,
  FOREIGN KEY (personId) REFERENCES customers(id)
);

-- sales_voucher_lines
CREATE TABLE IF NOT EXISTS sale_voucher_lines (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  voucherId INTEGER NOT NULL,
  itemId INTEGER NOT NULL,
  itemName TEXT NOT NULL,
  unit TEXT NOT NULL,
  qty DECIMAL(10, 2) NOT NULL,
  unitPrice DECIMAL(12, 0) NOT NULL,
  lineAmount DECIMAL(14, 2) NOT NULL,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (voucherId) REFERENCES sales_vouchers(id) ON DELETE CASCADE,
  FOREIGN KEY (itemId) REFERENCES items(id)
);

-- sales_debt (công nợ từ phieu xuat)
CREATE TABLE IF NOT EXISTS sale_debt (
  voucherId INTEGER PRIMARY KEY,
  personId INTEGER NOT NULL,
  debtPrincipal DECIMAL(14, 0) NOT NULL,
  debtInterest DECIMAL(12, 0) NOT NULL DEFAULT 0,
  debtTotal DECIMAL(14, 0) NOT NULL,
  createdDate DATE NOT NULL,
  lastInterestCalculatedDate DATE NOT NULL,
  isSettled BOOLEAN DEFAULT 0,
  FOREIGN KEY (voucherId) REFERENCES sales_vouchers(id) ON DELETE CASCADE,
  FOREIGN KEY (personId) REFERENCES customers(id)
);

-- purchase_debt (công nợ phải trả nhà cung cấp từ phiếu nhập)
CREATE TABLE IF NOT EXISTS purchase_debt (
  voucherId INTEGER PRIMARY KEY,
  personId INTEGER NOT NULL,
  debtPrincipal DECIMAL(14, 0) NOT NULL,
  debtInterest DECIMAL(12, 0) NOT NULL DEFAULT 0,
  debtTotal DECIMAL(14, 0) NOT NULL,
  createdDate DATE NOT NULL,
  lastInterestCalculatedDate DATE NOT NULL,
  isSettled BOOLEAN DEFAULT 0,
  FOREIGN KEY (voucherId) REFERENCES purchase_vouchers(id) ON DELETE CASCADE,
  FOREIGN KEY (personId) REFERENCES suppliers(id)
);

-- supplier_payments (phiếu trả nợ nhà cung cấp)
CREATE TABLE IF NOT EXISTS supplier_payments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  paymentDate DATE NOT NULL,
  personId INTEGER NOT NULL,
  personName TEXT NOT NULL,
  paidAmount DECIMAL(14, 0) NOT NULL,
  note TEXT,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deletedAt TIMESTAMP NULL,
  FOREIGN KEY (personId) REFERENCES suppliers(id)
);

-- supplier_payment_details
CREATE TABLE IF NOT EXISTS supplier_payment_details (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  paymentId INTEGER NOT NULL,
  voucherId INTEGER NOT NULL,
  debtBeforePrincipal DECIMAL(14, 0) NOT NULL,
  debtBeforeInterest DECIMAL(12, 0) NOT NULL,
  debtBeforeTotal DECIMAL(14, 0) NOT NULL,
  paidInterest DECIMAL(12, 0) NOT NULL DEFAULT 0,
  paidPrincipal DECIMAL(14, 0) NOT NULL,
  remainingPrincipal DECIMAL(14, 0) NOT NULL,
  remainingInterest DECIMAL(12, 0) NOT NULL DEFAULT 0,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (paymentId) REFERENCES supplier_payments(id) ON DELETE CASCADE,
  FOREIGN KEY (voucherId) REFERENCES purchase_vouchers(id)
);

-- debt_collections (phiếu thu nợ)
CREATE TABLE IF NOT EXISTS debt_collections (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  paymentDate DATE NOT NULL,
  personId INTEGER NOT NULL,
  personName TEXT NOT NULL,
  collectedAmount DECIMAL(14, 0) NOT NULL,
  note TEXT,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deletedAt TIMESTAMP NULL,
  FOREIGN KEY (personId) REFERENCES customers(id)
);

-- debt_collection_details (chi tiết từng khoản nợ được thu)
CREATE TABLE IF NOT EXISTS debt_collection_details (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  receiptId INTEGER NOT NULL,
  voucherId INTEGER NOT NULL,
  debtBeforePrincipal DECIMAL(14, 0) NOT NULL,
  debtBeforeInterest DECIMAL(12, 0) NOT NULL,
  debtBeforeTotal DECIMAL(14, 0) NOT NULL,
  collectedInterest DECIMAL(12, 0) NOT NULL DEFAULT 0,
  collectedPrincipal DECIMAL(14, 0) NOT NULL,
  remainingPrincipal DECIMAL(14, 0) NOT NULL,
  remainingInterest DECIMAL(12, 0) NOT NULL DEFAULT 0,
  createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (receiptId) REFERENCES debt_collections(id) ON DELETE CASCADE,
  FOREIGN KEY (voucherId) REFERENCES sales_vouchers(id)
);