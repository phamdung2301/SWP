package com.liteflow.modules.hr.dao;

import com.liteflow.modules.core.dao.GenericDAO;
import com.liteflow.modules.auth.model.ShiftTemplate;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ShiftTemplateDAO extends GenericDAO<ShiftTemplate, java.util.UUID> {

    public ShiftTemplateDAO() {
        super(ShiftTemplate.class, java.util.UUID.class);
    }

    public List<ShiftTemplate> findActiveOrdered() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT t FROM ShiftTemplate t WHERE t.isActive = true ORDER BY t.startTime ASC", ShiftTemplate.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}


