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

    public static MemberRepository createMemberRepository(
            EntityManager entityManager) {
        return new MemberRepository(entityManager);
    }

    public static UserRepository createUserRepository(
            EntityManager entityManager) {
        return new UserRepository(entityManager);
    }

    public static LoanRepository createLoanRepository(
            EntityManager entityManager) {
        return new LoanRepository(entityManager);
    }
}
