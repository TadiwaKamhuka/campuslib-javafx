package com.team.campuslib.util;

import jakarta.persistence.EntityManager;

import java.util.function.Function;

public final class Transactions {

    private Transactions() {
    }

    public static <T> T read(Function<EntityManager, T> work) {
        EntityManager em = DatabaseManager.getInstance().createEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    public static <T> T write(Function<EntityManager, T> work) {
        EntityManager em = DatabaseManager.getInstance().createEntityManager();
        var tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
