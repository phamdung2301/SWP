package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.TableSession;
import java.util.UUID;

/**
 * TableSessionDAO - DAO for managing TableSession entities
 */
public class TableSessionDAO extends GenericDAO<TableSession, UUID> {
    
    public TableSessionDAO() {
        super(TableSession.class, UUID.class);
    }
}
