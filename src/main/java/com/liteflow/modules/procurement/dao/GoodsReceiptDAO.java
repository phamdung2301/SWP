package com.liteflow.modules.procurement.dao;

import com.liteflow.modules.procurement.model.GoodsReceipt;
import java.util.UUID;

public class GoodsReceiptDAO extends GenericDAO<GoodsReceipt, UUID> {
    public GoodsReceiptDAO() { 
        super(GoodsReceipt.class); 
    }
}
