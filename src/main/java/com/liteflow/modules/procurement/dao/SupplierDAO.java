package com.liteflow.modules.procurement.dao;

import com.liteflow.modules.procurement.model.Supplier;
import java.util.UUID;

public class SupplierDAO extends GenericDAO<Supplier, UUID> {
    public SupplierDAO() { 
        super(Supplier.class); 
    }
}
