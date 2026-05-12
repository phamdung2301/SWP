package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.Inventory;
import java.util.UUID;

public class InventoryDAO extends GenericDAO<Inventory, UUID> {

    public InventoryDAO() {
        super(Inventory.class, UUID.class);
    }
}
