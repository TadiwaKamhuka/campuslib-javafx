
package com.team.campuslib.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class DatabaseManager {

    private static final String PERSISTENCE_UNIT_NAME =
            "campuslib-persistence";

    private final EntityManagerFactory entityManagerFactory;

    private DatabaseManager() {
        entityManagerFactory =
                Persistence.createEntityManagerFactory(
                        PERSISTENCE_UNIT_NAME
                );
    }

    private static class Holder {
        private static final DatabaseManager INSTANCE =
                new DatabaseManager();
    }

    public static DatabaseManager getInstance() {
        return Holder.INSTANCE;
    }

    public EntityManager createEntityManager() {
        if (!entityManagerFactory.isOpen()) {
            throw new IllegalStateException(
                    "The database connection factory is closed."
            );
        }

        return entityManagerFactory.createEntityManager();
    }

    public void shutdown() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
