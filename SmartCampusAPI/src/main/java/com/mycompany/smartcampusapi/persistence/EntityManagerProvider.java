package com.mycompany.smartcampusapi.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class EntityManagerProvider {

    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("my_persistence_unit");

    private EntityManagerProvider() {
    }

    public static EntityManager createEntityManager() {
        return FACTORY.createEntityManager();
    }
}
