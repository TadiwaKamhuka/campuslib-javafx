package com.team.campuslib.service;

import com.team.campuslib.model.Book;
import com.team.campuslib.repository.BookRepository;
import com.team.campuslib.repository.RepositoryFactory;
import com.team.campuslib.util.Transactions;
import com.team.campuslib.observer.DataChangePublisher;
import com.team.campuslib.observer.DataChangeType;
import com.team.campuslib.util.SessionManager;
import com.team.campuslib.util.ValidationHelper;

import java.util.List;
import java.util.Optional;

public class BookService {

    public List<Book> getAllBooks() {
        return Transactions.read(em ->
                RepositoryFactory.createBookRepository(em).findAll());
    }

    public Optional<Book> getBookById(Long id) {
        requireValid(ValidationHelper.validateBookId(id));

        return Transactions.read(em ->
                RepositoryFactory.createBookRepository(em).findById(id));
    }

    public List<Book> searchBooks(String searchType, String query) {
        requireValid(ValidationHelper.validateSearch(searchType, query));

        return Transactions.read(em ->
                RepositoryFactory.createBookRepository(em)
                        .search(searchType, query));
    }

    public void addBook(Book book) {
        SessionManager.getInstance().requireAdmin();
        normalise(book);
        requireValid(ValidationHelper.validateBook(book));

        Transactions.write(em -> {
            BookRepository repository =
                    RepositoryFactory.createBookRepository(em);

            if (repository.existsByIsbn(book.getIsbn(), null)) {
                throw new IllegalArgumentException(
                        "A book with this ISBN already exists.");
            }

            repository.save(book);
            return null;
        });

        DataChangePublisher.publish(DataChangeType.BOOKS);
    }

    public Book updateBook(Book book) {
        SessionManager.getInstance().requireAdmin();
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException(
                    "Book and book ID must not be null.");
        }

        normalise(book);

        Book updated = Transactions.write(em -> {
            BookRepository repository =
                    RepositoryFactory.createBookRepository(em);

            Book existing = repository.findById(book.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Book not found with ID: " + book.getId()));

            int onLoan = existing.getTotalCopies()
                    - existing.getAvailableCopies();

            if (book.getTotalCopies() < onLoan) {
                throw new IllegalArgumentException(
                        "Total copies cannot be lower than the "
                                + onLoan + " copies currently on loan.");
            }

            book.setAvailableCopies(book.getTotalCopies() - onLoan);
            requireValid(ValidationHelper.validateBook(book));

            if (repository.existsByIsbn(book.getIsbn(), book.getId())) {
                throw new IllegalArgumentException(
                        "A book with this ISBN already exists.");
            }

            return repository.update(book);
        });

        DataChangePublisher.publish(DataChangeType.BOOKS);
        return updated;
    }

    public void deleteBook(Long id) {
        SessionManager.getInstance().requireAdmin();
        requireValid(ValidationHelper.validateBookId(id));

        Transactions.write(em -> {
            BookRepository repository =
                    RepositoryFactory.createBookRepository(em);

            if (repository.findById(id).isEmpty()) {
                throw new IllegalArgumentException(
                        "Book not found with ID: " + id);
            }

            if (repository.countLoans(id) > 0) {
                throw new IllegalStateException(
                        "This book has loan records and cannot be deleted.");
            }

            repository.deleteById(id);
            return null;
        });

        DataChangePublisher.publish(DataChangeType.BOOKS);
    }

    private static void normalise(Book book) {
        if (book == null) {
            return;
        }

        if (book.getTitle() != null) {
            book.setTitle(book.getTitle().trim());
        }

        if (book.getAuthor() != null) {
            book.setAuthor(book.getAuthor().trim());
        }

        book.setIsbn(blankToNull(book.getIsbn()));
        book.setCategory(blankToNull(book.getCategory()));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static void requireValid(String error) {
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
    }
}
