package com.team.campuslib.repository;

import com.team.campuslib.model.User;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<User> findByUsername(String username) {
        List<User> result = entityManager
                .createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.username) = :name",
                        User.class)
                .setParameter("name", username.toLowerCase())
                .getResultList();

        return result.stream().findFirst();
    }

    public long count() {
        return entityManager
                .createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
    }

    public void save(User user) {
        entityManager.persist(user);
    }
}
