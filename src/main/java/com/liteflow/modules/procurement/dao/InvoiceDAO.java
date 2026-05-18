package com.liteflow.modules.procurement.dao;

import com.liteflow.modules.procurement.model.Invoice;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class InvoiceDAO extends GenericDAO<Invoice, UUID> {
    public InvoiceDAO() { 
        super(Invoice.class); 
    }
}
