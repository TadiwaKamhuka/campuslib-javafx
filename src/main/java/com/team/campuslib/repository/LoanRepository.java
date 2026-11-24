package com.team.campuslib.repository;

import com.team.campuslib.model.Loan;
import com.team.campuslib.model.LoanStatus;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LoanRepository {

    private static final String FETCH =
            "SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.member ";

    private final EntityManager entityManager;

    public LoanRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Loan> findById(Long id) {
        List<Loan> result = entityManager
                .createQuery(FETCH + "WHERE l.id = :id", Loan.class)
                .setParameter("id", id)
                .getResultList();

        return result.stream().findFirst();
    }

    public List<Loan> findAll() {
        return entityManager
                .createQuery(
                        FETCH + "ORDER BY l.borrowDate DESC, l.id DESC",
                        Loan.class)
                .getResultList();
    }

    public List<Loan> findOpen() {
        return entityManager
                .createQuery(
                        FETCH + "WHERE l.returnDate IS NULL ORDER BY l.dueDate",
                        Loan.class)
                .getResultList();
    }

    public List<Loan> findOverdue(LocalDate today) {
        return entityManager
                .createQuery(
                        FETCH + "WHERE l.returnDate IS NULL AND l.dueDate < :today "
                                + "ORDER BY l.dueDate",
                        Loan.class)
                .setParameter("today", today)
                .getResultList();
    }

    public List<Loan> findByMember(Long memberId) {
        return entityManager
                .createQuery(
                        FETCH + "WHERE l.member.id = :id "
                                + "ORDER BY l.borrowDate DESC, l.id DESC",
                        Loan.class)
                .setParameter("id", memberId)
                .getResultList();
    }

    public boolean existsOpenLoan(Long bookId, Long memberId) {
        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Loan l WHERE l.book.id = :b "
                                + "AND l.member.id = :m AND l.returnDate IS NULL",
                        Long.class)
                .setParameter("b", bookId)
                .setParameter("m", memberId)
                .getSingleResult();

        return count > 0;
    }

    public long countOpenByMember(Long memberId) {
        return entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Loan l WHERE l.member.id = :m "
                                + "AND l.returnDate IS NULL",
                        Long.class)
                .setParameter("m", memberId)
                .getSingleResult();
    }

    public long countOpen() {
        return entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Loan l WHERE l.returnDate IS NULL",
                        Long.class)
                .getSingleResult();
    }

    public long countOverdue(LocalDate today) {
        return entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Loan l WHERE l.returnDate IS NULL "
                                + "AND l.dueDate < :today",
                        Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }

    public int markOverdue(LocalDate today) {
        return entityManager
                .createQuery(
                        "UPDATE Loan l SET l.status = :overdue "
                                + "WHERE l.returnDate IS NULL AND l.dueDate < :today "
                                + "AND l.status = :active")
                .setParameter("overdue", LoanStatus.OVERDUE)
                .setParameter("active", LoanStatus.ACTIVE)
                .setParameter("today", today)
                .executeUpdate();
    }

    public void save(Loan loan) {
        entityManager.persist(loan);
    }
}
