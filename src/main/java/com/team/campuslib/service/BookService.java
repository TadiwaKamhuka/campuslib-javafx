
package com.team.campuslib.service;

import com.team.campuslib.model.Book;
import com.team.campuslib.repository.BookRepository;
import com.team.campuslib.repository.RepositoryFactory;
import com.team.campuslib.util.DatabaseManager;
import com.team.campuslib.util.ValidationHelper;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class BookService {

    public List<Book> getAllBooks() {
        EntityManager entityManager =
                DatabaseManager.getInstance().createEntityManager();

        try {
            BookRepository repository =
                    RepositoryFactory.createBookRepository(entityManager);

            return repository.findAll();

        } finally {
            entityManager.close();
        }
    }

    public Optional<Book> getBookById(Long id) {
        String error = ValidationHelper.validateBookId(id);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        EntityManager entityManager =
                DatabaseManager.getInstance().createEntityManager();

        try {
            BookRepository repository =
                    RepositoryFactory.createBookRepository(entityManager);

            return repository.findById(id);

        } finally {
            entityManager.close();
        }
    }

    public List<Book> searchBooks(String searchType, String query) {
        String error =
                ValidationHelper.validateSearch(searchType, query);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        EntityManager entityManager =
                DatabaseManager.getInstance().createEntityManager();

        try {
            BookRepository repository =
                    RepositoryFactory.createBookRepository(entityManager);

            return repository.search(searchType, query);

        } finally {
            entityManager.close();
        }
    }

    public void addBook(Book book) {
        String error = ValidationHelper.validateBook(book);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        EntityManager entityManager =
                DatabaseManager.getInstance().createEntityManager();

        var transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            BookRepository repository =
                    RepositoryFactory.createBookRepository(entityManager);

            repository.save(book);

            transaction.commit();

        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            entityManager.close();
        }
    }

    public Book updateBook(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException(
                    "Book and book ID must not be null."
            );
        }

        String error = ValidationHelper.validateBook(book);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        EntityManager entityManager =
                DatabaseManager.getInstance().createEntityManager();

        var transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            BookRepository repository =
                    RepositoryFactory.createBookRepository(entityManager);

            Optional<Book> existingBook =
                    repository.findById(book.getId());

            if (existingBook.isEmpty()) {
                throw new IllegalArgumentException(
                        "Book not found with ID: " + book.getId()
                );
            }

            // Preserve the database-controlled available copy count.
            book.setAvailableCopies(
                    existingBook.get().getAvailableCopies()
            );

            Book updatedBook = repository.update(book);

            transaction.commit();

            return updatedBook;

        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            entityManager.close();
        }
    }

    public void deleteBook(Long id) {
        String error = ValidationHelper.validateBookId(id);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        EntityManager entityManager =
                DatabaseManager.getInstance().createEntityManager();

        var transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            BookRepository repository =
                    RepositoryFactory.createBookRepository(entityManager);

            if (repository.findById(id).isEmpty()) {
                throw new IllegalArgumentException(
                        "Book not found with ID: " + id
                );
            }

            repository.deleteById(id);

            transaction.commit();

        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            entityManager.close();
        }
    }
}

