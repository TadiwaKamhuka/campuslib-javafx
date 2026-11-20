
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
}
