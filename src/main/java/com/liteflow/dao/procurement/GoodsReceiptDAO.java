package com.liteflow.dao.procurement;

import com.liteflow.model.procurement.GoodsReceipt;
import java.util.UUID;

public class GoodsReceiptDAO extends GenericDAO<GoodsReceipt, UUID> {
    public GoodsReceiptDAO() { 
        super(GoodsReceipt.class); 
    }
}
