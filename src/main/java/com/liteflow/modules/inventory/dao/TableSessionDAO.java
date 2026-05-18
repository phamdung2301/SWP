package com.liteflow.modules.inventory.dao;

import com.liteflow.modules.core.dao.GenericDAO;
import com.liteflow.modules.inventory.model.TableSession;
import java.util.UUID;

/**
 * TableSessionDAO - DAO for managing TableSession entities
 */
public class TableSessionDAO extends GenericDAO<TableSession, UUID> {
    
    public TableSessionDAO() {
        super(TableSession.class, UUID.class);
    }
}
