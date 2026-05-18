package com.liteflow.modules.hr.dao;

import com.liteflow.modules.core.dao.GenericDAO;
import com.liteflow.modules.hr.model.PayPeriod;
import java.util.UUID;

/**
 * DAO for PayPeriod operations
 */
public class PayPeriodDAO extends GenericDAO<PayPeriod, UUID> {

    public PayPeriodDAO() {
        super(PayPeriod.class, UUID.class);
    }
}

