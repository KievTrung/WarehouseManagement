package com.KievTrung.core.service;

import com.KievTrung.core.domain.Debt;
import com.KievTrung.core.domain.Person;
import com.KievTrung.core.domain.Voucher;
import com.KievTrung.util.helper.DaoHelper;
import lombok.Cleanup;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DebtService extends DaoHelper<Debt> {
//  - [ ] calculateInterest(debtPrincipal, annualRate, dayOverdue) → decimal
//  - [ ] updateDebtInterest(voucherNo) → recalc interest
//  - [ ] fullyDebtByVoucher(voucherNo) → bool

    public void executeUpdateInterest(Person.Type type, List<Integer> voucherIds){
        wrapTransaction(new SqlAction<>(){
            @Override
            public void execTransaction(Connection con) throws SQLException {
                List<PreparedStatement> psI = DebtService.updateDebtInterest(con, type == Person.Type.CUSTOMER, voucherIds);
                for (PreparedStatement ps : psI){
                    @Cleanup PreparedStatement temp = ps;
                    temp.executeBatch();
                }
            }
        });
    }

    public static BigDecimal calculateInterestTillNow(BigDecimal debtPrincipal,
                                                      BigDecimal rate,
                                                      Voucher.Type rateType,
                                                      LocalDate start) {
        BigDecimal dayOverDue = new BigDecimal(ChronoUnit.DAYS.between(start, LocalDate.now()));
        // interest = debtPrincipal x dailyRate x dayOverDue
        return debtPrincipal
                .multiply(InterestCalculationService.convertSimpleDaily(rate, rateType))
                .multiply(dayOverDue);
    }

    public static List<PreparedStatement> updateDebtInterest(Connection con, boolean isCustomer, List<Integer> voucherIds) throws SQLException {
        String debtTable = isCustomer ? "sale" : "purchase";
        // get debt info of voucher
        @Cleanup PreparedStatement ps = con.prepareStatement(
                "select debtPrincipal, rate, rateType, lastInterestCalculatedDate as lastTime " +
                        String.format("from %s_vouchers pv inner join %s_debt pd on pd.voucherId = pv.id ", debtTable, debtTable) +
                        "where pv.id = ?"
        );
        // update interest
        PreparedStatement psImodify = con.prepareStatement(
                String.format("update %s_debt ", debtTable) +
                        "set debtInterest = debtInterest + ?, lastInterestCalculatedDate = ? " +
                        "where voucherId = ?"
        );
        // update interest
        PreparedStatement psI = con.prepareStatement(
                String.format("update %s_debt set lastInterestCalculatedDate = ? where voucherId = ?", debtTable)
        );

        List<PreparedStatement> pss = new ArrayList<>();
        for (int id : voucherIds){
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            rs.next();
            BigDecimal debtPrincipal = rs.getBigDecimal("debtPrincipal");
            BigDecimal rate = rs.getBigDecimal("rate");
            Voucher.Type type = Voucher.Type.valueOf(rs.getString("rateType"));
            LocalDate lastTime = rs.getDate("lastTime").toLocalDate();

            if (rate.compareTo(BigDecimal.ZERO) > 0) {
                //calculate interest based on the last time paid
                BigDecimal interest = calculateInterestTillNow(debtPrincipal, rate, type, lastTime);

                psImodify.setBigDecimal(1, interest);
                psImodify.setDate(2, Date.valueOf(LocalDate.now()));
                psImodify.setInt(3, id);
                psImodify.addBatch();
            } else {
                psI.setDate(1, Date.valueOf(LocalDate.now()));
                psI.setInt(2, id);
                psI.addBatch();
            }
        }
        pss.add(psImodify);
        pss.add(psI);
        return pss;
    }
}
