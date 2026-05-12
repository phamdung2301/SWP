package com.liteflow.dao.payroll;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.payroll.PayrollRun;
import java.util.UUID;

/**
 * DAO for PayrollRun operations
 */
public class PayrollRunDAO extends GenericDAO<PayrollRun, UUID> {

    public PayrollRunDAO() {
        super(PayrollRun.class, UUID.class);
    }
}

