
package com.team.campuslib.repository;

import jakarta.persistence.EntityManager;

public class RepositoryFactory {

    private RepositoryFactory() {
        // Prevent direct instantiation.
    }

    public static BookRepository createBookRepository(
            EntityManager entityManager) {

        return new BookRepository(entityManager);
    }
}
