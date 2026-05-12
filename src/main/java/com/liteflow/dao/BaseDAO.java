/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.liteflow.dao;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public abstract class BaseDAO<T, ID> {

    public static EntityManagerFactory emf;
    
    static {
        try {
            emf = Persistence.createEntityManagerFactory("LiteFlowPU");
        } catch (Exception e) {
            System.err.println("❌ Failed to create EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            emf = null;
        }
    }

    public abstract List<T> getAll();

    public abstract T findById(ID id);

    public abstract boolean insert(T t);

    public abstract boolean update(T t);

    public abstract boolean delete(ID id);

    
}
