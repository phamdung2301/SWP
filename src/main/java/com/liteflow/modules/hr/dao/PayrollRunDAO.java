package com.liteflow.modules.hr.dao;

import com.liteflow.modules.core.dao.GenericDAO;
import com.liteflow.modules.hr.model.PayrollRun;
import java.util.UUID;

/**
 * DAO for PayrollRun operations
 */
public class PayrollRunDAO extends GenericDAO<PayrollRun, UUID> {

    public PayrollRunDAO() {
        super(PayrollRun.class, UUID.class);
    }
}

