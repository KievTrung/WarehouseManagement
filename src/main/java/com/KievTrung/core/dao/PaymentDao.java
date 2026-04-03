package com.KievTrung.core.dao;

import com.KievTrung.core.domain.Debt;
import com.KievTrung.core.domain.Payment;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.repository.RepositoryI;
import com.KievTrung.core.service.DebtAllocationService;
import com.KievTrung.core.service.DebtService;
import com.KievTrung.util.helper.DaoHelper;
import lombok.Cleanup;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentDao extends DaoHelper<Payment> implements RepositoryI<Payment> {

    public void create(Person.Type type, Payment payment) {
        wrapTransaction(new SqlAction<>() {
            //step 1: create record in payment table
            //step 2: collect all unpaid debt of person

            //step 0: check if payment date is after voucher create date

            //step 2.1: update interest for each voucher
            //step 3: (paid interest, paid the pricipal) from oldest to lastest
            //step 4: create payment detail record
            //step 5: update back principle and interest in purchase debt table for each payment detail
            @Override
            public void execTransaction(Connection con) throws SQLException {
                boolean isCustomer = type == Person.Type.CUSTOMER;
                String debtTable = isCustomer ? "sale_debt" : "purchase_debt";
                Timestamp create = Timestamp.valueOf(payment.getCreateDate());
                Timestamp update = Timestamp.valueOf(payment.getUpdateDate());

                // step 1:
                @Cleanup PreparedStatement psPayment = con.prepareStatement(
                        String.format("insert into %s (paymentDate, personId, personName, paidAmount, note, createDate, updateDate, deletedAt) values (?,?,?,?,?,?,?,?)", isCustomer ? "debt_collections" : "supplier_payments")
                );
                psPayment.setDate(1, Date.valueOf(payment.getPaymentDate()));
                psPayment.setInt(2, payment.getPersonId());
                psPayment.setString(3, payment.getPersonName());
                psPayment.setBigDecimal(4, payment.getPaidAmount());
                psPayment.setString(5, payment.getNote());
                psPayment.setTimestamp(6, create);
                psPayment.setTimestamp(7, update);
                psPayment.setTimestamp(8, null);
                int paymentId = DaoHelper.executeAndGetId(psPayment);

                // step 2:
                @Cleanup PreparedStatement psCollectDebt = con.prepareStatement(
                        "select voucherId, debtPrincipal, debtInterest, createDate " +
                                String.format("from %s where personId = ? and isSettled = 0", debtTable)
                );

                psCollectDebt.setInt(1, payment.getPersonId());
                ResultSet rs = psCollectDebt.executeQuery();
                List<Debt> debts = new ArrayList<>();
                while (rs.next()) {
                    Debt debt = new Debt();
                    debt.setVoucherId(rs.getInt("voucherId"));
                    debt.setDebtPrincipal(rs.getBigDecimal("debtPrincipal"));
                    debt.setDebtInterest(rs.getBigDecimal("debtInterest"));

                    // step 0:
                    if (payment.getPaymentDate().isBefore(rs.getDate("createDate").toLocalDate()))
                        throw new SQLException("Phát hiện ngày thanh toán trước ngày tạo phiếu");

                    debts.add(debt);
                }
                // step 2.1:
                List<PreparedStatement> psI = DebtService.updateDebtInterest(con, isCustomer, debts.stream().map(Debt::getVoucherId).toList());
                for (PreparedStatement ps : psI){
                    @Cleanup PreparedStatement temp = ps;
                    temp.executeBatch();
                }

                // step 3:
                debts.sort(Collections.reverseOrder());
                List<DebtAllocationService.Allocation> allocations = DebtAllocationService.debtAllocate(payment.getPaidAmount(), debts);
                // step 4:
                @Cleanup PreparedStatement psA = con.prepareStatement(
                        "insert into supplier_payment_details values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );
                // step 5:
                @Cleanup PreparedStatement psD = con.prepareStatement(
                        "update purchase_debt " +
                                "set debtPrincipal = ?, debtInterest = ?, debtTotal = ?, isSettled = ? " +
                                "where voucherId = ?");

                for (DebtAllocationService.Allocation a : allocations) {
                    psA.setInt(1, paymentId);
                    psA.setInt(2, a.voucherId);
                    psA.setBigDecimal(3, a.debtPrincipleBeforeApply);
                    psA.setBigDecimal(4, a.debtInterestBeforeApply);
                    psA.setBigDecimal(5, a.debtTotalBeforeApply);
                    psA.setBigDecimal(6, a.paidInterest);
                    psA.setBigDecimal(7, a.paidPrincipal);
                    psA.setBigDecimal(8, a.remainingPrincipal);
                    psA.setBigDecimal(9, a.remainingInterest);
                    psA.setTimestamp(10, create);
                    psA.setTimestamp(11, update);
                    psA.addBatch();

                    psD.setBigDecimal(1, a.remainingPrincipal);
                    psD.setBigDecimal(2, a.remainingInterest);
                    psD.setBigDecimal(3, a.remainingPrincipal.add(a.remainingInterest));
                    psD.setBoolean(4, a.isSettled);
                    psD.setInt(5, a.voucherId);
                    psD.addBatch();
                }
                psA.executeBatch();
                psD.executeBatch();
            }
        });
    }

    @Override
    public void create(Person.Type type, List<Payment> payments) {
        wrapTransaction(new SqlAction<>() {
            //step 1: create record in supplier_payment
            //step 2: collect all unpaid debt of supplier or customer
            //step 2.1: update interest for each voucher
            //step 3: (paid interest, paid the pricipal) from oldest to lastest
            //step 4: create payment detail record
            //step 5: update back principle and interest in purchase debt table for each payment detail
            @Override
            public void execTransaction(Connection con) throws SQLException {
                boolean isCustomer = type == Person.Type.CUSTOMER;

                @Cleanup PreparedStatement psPayment = con.prepareStatement(
                        String.format("insert into %s (id, paymentDate, personId, personName, paidAmount, note, createDate, updateDate, deletedAt) values (?,?,?,?,?,?,?,?)", isCustomer ? "debt_collections" : "supplier_payments")
                );
                @Cleanup PreparedStatement psCollectDebt = con.prepareStatement(
                        "select voucherId, debtPrincipal, debtInterest, createDate " +
                                String.format("from %s_debt where personId = ? and isSettled = 0", isCustomer ? "sale" : "purchase")
                );
                List<PreparedStatement> psI = null;
                @Cleanup PreparedStatement psA = con.prepareStatement(
                        String.format("insert into %s_details values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", isCustomer ? "debt_collection" : "supplier_payment")
                );
                @Cleanup PreparedStatement psD = con.prepareStatement(
                        String.format("update %s_debt ", isCustomer ? "sale" : "purchase") +
                                "set debtPrincipal = ?, debtInterest = ?, debtTotal = ?, isSettled = ? " +
                                "where voucherId = ?"
                );
                for (Payment payment : payments) {
                    Timestamp create = Timestamp.valueOf(payment.getCreateDate());
                    Timestamp update = Timestamp.valueOf(payment.getUpdateDate());

                    // step 1:
                    psPayment.setInt(1, payment.getId());
                    psPayment.setDate(2, Date.valueOf(payment.getPaymentDate()));
                    psPayment.setInt(3, payment.getPersonId());
                    psPayment.setString(4, payment.getPersonName());
                    psPayment.setBigDecimal(5, payment.getPaidAmount());
                    psPayment.setString(6, payment.getNote());
                    psPayment.setTimestamp(7, create);
                    psPayment.setTimestamp(8, update);
                    psPayment.setTimestamp(9, null);
                    psPayment.addBatch();

                    // step 2:
                    psCollectDebt.setInt(1, payment.getPersonId());
                    ResultSet rs = psCollectDebt.executeQuery();
                    List<Debt> debts = new ArrayList<>();
                    List<Integer> voucherIds = new ArrayList<>();
                    while (rs.next()) {
                        Debt debt = new Debt();
                        Integer voucherId = rs.getInt("voucherId");
                        debt.setVoucherId(voucherId);
                        voucherIds.add(voucherId);
                        debt.setDebtPrincipal(rs.getBigDecimal("debtPrincipal"));
                        debt.setDebtInterest(rs.getBigDecimal("debtInterest"));
                        debts.add(debt);
                    }
                    // step 2.1:
                    psI = DebtService.updateDebtInterest(con, isCustomer, voucherIds);
                    // step 3:
                    debts.sort(Collections.reverseOrder());
                    List<DebtAllocationService.Allocation> allocations = DebtAllocationService.debtAllocate(payment.getPaidAmount(), debts);

                    for (DebtAllocationService.Allocation a : allocations) {
                        // step 4:
                        psA.setInt(1, payment.getId());
                        psA.setInt(2, a.voucherId);
                        psA.setBigDecimal(3, a.debtPrincipleBeforeApply);
                        psA.setBigDecimal(4, a.debtInterestBeforeApply);
                        psA.setBigDecimal(5, a.debtTotalBeforeApply);
                        psA.setBigDecimal(6, a.paidInterest);
                        psA.setBigDecimal(7, a.paidPrincipal);
                        psA.setBigDecimal(8, a.remainingPrincipal);
                        psA.setBigDecimal(9, a.remainingInterest);
                        psA.setTimestamp(10, create);
                        psA.setTimestamp(11, update);
                        psA.addBatch();

                        // step 5:
                        psD.setBigDecimal(1, a.remainingPrincipal);
                        psD.setBigDecimal(2, a.remainingInterest);
                        psD.setBigDecimal(3, a.remainingPrincipal.add(a.remainingInterest));
                        psD.setBoolean(4, a.isSettled);
                        psD.setInt(5, a.voucherId);
                        psD.addBatch();
                    }
                }
                psPayment.executeBatch();
                assert psI != null;
                for (PreparedStatement ps : psI){
                    @Cleanup PreparedStatement temp = ps;
                    temp.executeBatch();
                }
                psA.executeBatch();
                psD.executeBatch();
            }
        });
    }

    @Override
    public void update(Person.Type type, Payment obj) {

    }

    @Override
    public void delete(Person.Type type, Integer id) {

    }

    @Override
    public List<Payment> findAll(Person.Type type) {
        String sql = "select * from supplier_payments order by paymentDate desc";
        return wrapListResultReturnListT(new String[]{sql}, new SqlAction<>() {
            @Override
            public List<Payment> execListResultReturnListT(List<ResultSet> rss) throws SQLException {
                @Cleanup ResultSet rs = rss.getFirst();
                List<Payment> payments = new ArrayList<>();
                while (rs.next()) {
                    Timestamp deletedAt = rs.getTimestamp("deletedAt");
                    Payment payment = new Payment(
                            rs.getInt("id"),
                            rs.getDate("paymentDate").toLocalDate(),
                            rs.getInt("personId"),
                            rs.getString("personName"),
                            rs.getBigDecimal("paidAmount"),
                            rs.getString("note"),
                            rs.getTimestamp("createDate").toLocalDateTime(),
                            rs.getTimestamp("updateDate").toLocalDateTime(),
                            deletedAt == null ? null : deletedAt.toLocalDateTime()
                    );
                    payments.add(payment);
                }
                return payments;
            }
        });
    }

    @Override
    public List<Payment> findByName(Person.Type type, String name) {
        return List.of();
    }

    @Override
    public Payment findById(Person.Type type, Integer id) {
        return null;
    }

    @Override
    public List<Payment> findAllById(Person.Type type, Integer id) {
        return List.of();
    }

    @Override
    public List<Payment> findAllActiveById(Person.Type type, Integer id) {
        return List.of();
    }

}
