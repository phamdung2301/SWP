package com.liteflow.modules.procurement.dao;

import com.liteflow.modules.procurement.model.SupplierSLA;
import java.util.UUID;

public class SupplierSLADAO extends GenericDAO<SupplierSLA, UUID> {
    public SupplierSLADAO() { 
        super(SupplierSLA.class); 
    }
}
