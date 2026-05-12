package com.liteflow.dao.inventory;

import com.liteflow.dao.GenericDAO;
import com.liteflow.model.inventory.ProductVariant;
import java.util.List;
import java.util.UUID;

public class ProductVariantDAO extends GenericDAO<ProductVariant, UUID> {

    public ProductVariantDAO() {
        super(ProductVariant.class, UUID.class);
    }

    public ProductVariant findByProductAndSize(UUID productId, String size) {
        var em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT pv FROM ProductVariant pv WHERE pv.product.productId = :pid AND pv.size = :size AND (pv.isDeleted = false OR pv.isDeleted IS NULL)",
                    ProductVariant.class)
                .setParameter("pid", productId)
                .setParameter("size", size)
                .setMaxResults(1)
                .getResultList()
                .stream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
    
    public List<ProductVariant> findByProductId(UUID productId) {
        var em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT pv FROM ProductVariant pv WHERE pv.product.productId = :pid AND (pv.isDeleted = false OR pv.isDeleted IS NULL) ORDER BY pv.size",
                    ProductVariant.class)
                .setParameter("pid", productId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
