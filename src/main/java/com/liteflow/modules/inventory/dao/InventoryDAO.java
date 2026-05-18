package com.liteflow.modules.inventory.dao;

import com.liteflow.modules.core.dao.GenericDAO;
import com.liteflow.modules.inventory.model.Inventory;
import java.util.UUID;

public class InventoryDAO extends GenericDAO<Inventory, UUID> {

    public InventoryDAO() {
        super(Inventory.class, UUID.class);
    }
}
