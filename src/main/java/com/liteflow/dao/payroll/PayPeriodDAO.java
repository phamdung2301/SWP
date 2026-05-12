package com.liteflow.dao.payroll;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.payroll.PayPeriod;
import java.util.UUID;

/**
 * DAO for PayPeriod operations
 */
public class PayPeriodDAO extends GenericDAO<PayPeriod, UUID> {

    public PayPeriodDAO() {
        super(PayPeriod.class, UUID.class);
    }
}

