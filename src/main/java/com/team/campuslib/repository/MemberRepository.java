package com.team.campuslib.repository;

import com.team.campuslib.model.Member;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class MemberRepository {

    private final EntityManager entityManager;

    public MemberRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Member> findAll() {
        return entityManager
                .createQuery(
                        "SELECT m FROM Member m ORDER BY m.fullName",
                        Member.class)
                .getResultList();
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Member.class, id));
    }

    public List<Member> search(String query) {
        return entityManager
                .createQuery(
                        "SELECT m FROM Member m "
                                + "WHERE LOWER(m.fullName) LIKE :q "
                                + "OR LOWER(m.email) LIKE :q "
                                + "OR LOWER(m.phone) LIKE :q "
                                + "ORDER BY m.fullName",
                        Member.class)
                .setParameter("q", "%" + query.toLowerCase().trim() + "%")
                .getResultList();
    }

    public boolean existsByEmail(String email, Long excludeId) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(m) FROM Member m "
                                + "WHERE LOWER(m.email) = :email AND m.id <> :id",
                        Long.class)
                .setParameter("email", email.toLowerCase())
                .setParameter("id", excludeId == null ? -1L : excludeId)
                .getSingleResult();

        return count > 0;
    }

    public long countOpenLoans(Long memberId) {
        return entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Loan l "
                                + "WHERE l.member.id = :id AND l.returnDate IS NULL",
                        Long.class)
                .setParameter("id", memberId)
                .getSingleResult();
    }

    public long countLoans(Long memberId) {
        return entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Loan l WHERE l.member.id = :id",
                        Long.class)
                .setParameter("id", memberId)
                .getSingleResult();
    }

    public long count() {
        return entityManager
                .createQuery("SELECT COUNT(m) FROM Member m", Long.class)
                .getSingleResult();
    }

    public void save(Member member) {
        entityManager.persist(member);
    }

    public Member update(Member member) {
        return entityManager.merge(member);
    }

    public void deleteById(Long id) {
        Member member = entityManager.find(Member.class, id);

        if (member != null) {
            entityManager.remove(member);
        }
    }
}
