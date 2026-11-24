
package com.team.campuslib;

import com.team.campuslib.model.Book;
import com.team.campuslib.service.BookService;
import com.team.campuslib.util.DatabaseManager;

import java.util.List;
import java.util.Optional;

public class PersistenceSmokeTest {

    public static void main(String[] args) {

        BookService bookService = new BookService();
        com.team.campuslib.util.SeedDataInitializer.seedIfEmpty();
        new com.team.campuslib.service.AuthService().login("admin", "admin123");

        Long bookId = null;

        try {
            // STEP 1: CREATE A BOOK
            Book book = new Book(
                    "Backend Test Book",
                    "Rob",
                    "BACKEND-TEST-001",
                    "Testing",
                    3
            );

            bookService.addBook(book);

            bookId = book.getId();

            System.out.println("\n1. BOOK CREATED");
            System.out.println("ID: " + bookId);

            // STEP 2: RETRIEVE ALL BOOKS
            List<Book> books = bookService.getAllBooks();

            System.out.println("\n2. ALL BOOKS RETRIEVED");
            System.out.println("Total books: " + books.size());

            // STEP 3: RETRIEVE BOOK BY ID
            Optional<Book> retrievedBook =
                    bookService.getBookById(bookId);

            if (retrievedBook.isEmpty()) {
                throw new RuntimeException("Book retrieval failed!");
            }

            System.out.println("\n3. BOOK RETRIEVED BY ID");
            System.out.println("Title: " + retrievedBook.get().getTitle());

            // STEP 4: TEST SEARCH STRATEGIES
            testSearch(bookService, "title", "Backend Test Book");
            testSearch(bookService, "author", "Rob");
            testSearch(bookService, "isbn", "BACKEND-TEST-001");
            testSearch(bookService, "category", "Testing");

            // STEP 5: UPDATE BOOK
            Book updatedBook = retrievedBook.get();

            updatedBook.setTitle("Updated Backend Test Book");

            bookService.updateBook(updatedBook);

            Optional<Book> afterUpdate =
                    bookService.getBookById(bookId);

            if (afterUpdate.isEmpty()
                    || !afterUpdate.get().getTitle()
                    .equals("Updated Backend Test Book")) {
                throw new RuntimeException("Book update failed!");
            }

            System.out.println("\n4. BOOK UPDATED SUCCESSFULLY");

            // STEP 6: VERIFY AVAILABLE COPIES
            System.out.println(
                    "Available copies: "
                            + afterUpdate.get().getAvailableCopies()
            );

            // STEP 7: VERIFY PERSISTENCE
            Optional<Book> persistedBook =
                    bookService.getBookById(bookId);

            if (persistedBook.isEmpty()) {
                throw new RuntimeException("Persistence check failed!");
            }

            System.out.println("\n5. BOOK PERSISTENCE VERIFIED");

            // STEP 8: DELETE BOOK
            bookService.deleteBook(bookId);

            if (bookService.getBookById(bookId).isPresent()) {
                throw new RuntimeException("Book deletion failed!");
            }

            System.out.println("\n6. BOOK DELETED SUCCESSFULLY");

            System.out.println("\nALL BOOK BACKEND TESTS PASSED!");

        } finally {

            // CLEANUP IF A PREVIOUS TEST FAILED
            if (bookId != null) {
                try {
                    bookService.deleteBook(bookId);
                } catch (RuntimeException ignored) {
                    // The book may already have been deleted.
                }
            }

            DatabaseManager.getInstance().shutdown();
        }
    }

    private static void testSearch(
            BookService bookService,
            String searchType,
            String query
    ) {

        List<Book> results =
                bookService.searchBooks(searchType, query);

        if (results.isEmpty()) {
            throw new RuntimeException(
                    "Search failed for: " + searchType
            );
        }

        System.out.println(
                "Search passed: " + searchType
        );
    }
}
