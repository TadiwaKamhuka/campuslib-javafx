
package com.team.campuslib.repository;

import com.team.campuslib.model.Book;
import com.team.campuslib.strategy.BookSearchStrategy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class BookRepository {

    private final EntityManager entityManager;

    public BookRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Book> findAll() {
        return entityManager
                .createQuery(
                        "SELECT b FROM Book b ORDER BY b.title",
                        Book.class
                )
                .getResultList();
    }

    public Optional<Book> findById(Long id) {
        Book book = entityManager.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    public void save(Book book) {
        entityManager.persist(book);
    }

    public Book update(Book book) {
        return entityManager.merge(book);
    }

    public void deleteById(Long id) {
        Book book = entityManager.find(Book.class, id);

        if (book != null) {
            entityManager.remove(book);
        }
    }

    public List<Book> search(String searchType, String query) {

        BookSearchStrategy strategy =
                BookSearchStrategy.from(searchType);

        String jpql = strategy.getSearchQuery();

        TypedQuery<Book> typedQuery =
                entityManager.createQuery(jpql, Book.class);

        typedQuery.setParameter(
                "query",
                "%" + query.toLowerCase().trim() + "%"
        );

        return typedQuery.getResultList();
    }

    public boolean existsByIsbn(String isbn, Long excludeId) {
        if (isbn == null) {
            return false;
        }

        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(b) FROM Book b "
                                + "WHERE LOWER(b.isbn) = :isbn AND b.id <> :id",
                        Long.class)
                .setParameter("isbn", isbn.toLowerCase())
                .setParameter("id", excludeId == null ? -1L : excludeId)
                .getSingleResult();

        return count > 0;
    }

    public long countLoans(Long bookId) {
        return entityManager
                .createQuery(
                        "SELECT COUNT(l) FROM Loan l WHERE l.book.id = :id",
                        Long.class)
                .setParameter("id", bookId)
                .getSingleResult();
    }

    public long count() {
        return entityManager
                .createQuery("SELECT COUNT(b) FROM Book b", Long.class)
                .getSingleResult();
    }
}
