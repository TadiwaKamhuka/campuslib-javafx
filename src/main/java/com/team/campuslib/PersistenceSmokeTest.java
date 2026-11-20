
package com.team.campuslib;

import com.team.campuslib.model.Book;
import com.team.campuslib.util.DatabaseManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class PersistenceSmokeTest {

    public static void main(String[] args) {

        DatabaseManager databaseManager = DatabaseManager.getInstance();

        Long bookId = null;

        try {
            // STEP 1: Create a Book
            Book book = new Book(
                    "Persistence Test",
                    "Rob",
                    "SMOKE-TEST-20261118",
                    "Testing",
                    3
            );

            // STEP 2: Open EntityManager
            EntityManager em = databaseManager.createEntityManager();

            try {
                // STEP 3: Begin transaction
                EntityTransaction transaction = em.getTransaction();
                transaction.begin();

                // STEP 4: Save the Book
                em.persist(book);

                // STEP 5: Commit transaction
                transaction.commit();

                bookId = book.getId();

                System.out.println("BOOK SAVED! ID: " + bookId);

            } finally {
                em.close();
            }

            // STEP 6: Retrieve the Book using a NEW EntityManager
            EntityManager readEm = databaseManager.createEntityManager();

            try {
                Book retrievedBook = readEm.find(Book.class, bookId);

                if (retrievedBook != null) {
                    System.out.println("BOOK RETRIEVED SUCCESSFULLY!");
                    System.out.println("Title: " + retrievedBook.getTitle());
                    System.out.println("Author: " + retrievedBook.getAuthor());
                    System.out.println("Available copies: "
                            + retrievedBook.getAvailableCopies());
                } else {
                    throw new RuntimeException("Book was not found!");
                }

                // STEP 7: Delete the temporary test record
                EntityTransaction transaction = readEm.getTransaction();
                transaction.begin();

                readEm.remove(retrievedBook);

                transaction.commit();

                System.out.println("TEST RECORD CLEANED UP!");

            } finally {
                readEm.close();
            }

        } finally {
            databaseManager.shutdown();
        }
    }
}
