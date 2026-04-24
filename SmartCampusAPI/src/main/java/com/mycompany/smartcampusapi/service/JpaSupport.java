package com.mycompany.smartcampusapi.service;

import java.util.function.Function;

import com.mycompany.smartcampusapi.persistence.EntityManagerProvider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

abstract class JpaSupport {

    protected <T> T execute(Function<EntityManager, T> work) {
        EntityManager entityManager = EntityManagerProvider.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            T result = work.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }
}