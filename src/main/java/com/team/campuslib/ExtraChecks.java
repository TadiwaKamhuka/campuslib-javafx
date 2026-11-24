package com.team.campuslib;

import com.team.campuslib.model.Book;
import com.team.campuslib.model.Role;
import com.team.campuslib.observer.DataChangeListener;
import com.team.campuslib.observer.DataChangePublisher;
import com.team.campuslib.observer.DataChangeType;
import com.team.campuslib.service.AuthService;
import com.team.campuslib.service.BookService;
import com.team.campuslib.util.DatabaseManager;
import com.team.campuslib.util.SeedDataInitializer;
import com.team.campuslib.util.Transactions;

import java.util.ArrayList;
import java.util.List;

public class ExtraChecks {

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        System.out.println("PASS: " + name);
    }

    private static void expectError(String name, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            System.out.println("PASS: " + name + " -> " + e.getMessage());
            return;
        }
        throw new AssertionError("FAIL: " + name);
    }

    public static void main(String[] args) {
        SeedDataInitializer.seedIfEmpty();

        AuthService auth = new AuthService();
        BookService books = new BookService();
        String tag = String.valueOf(System.nanoTime());
        String username = "stu" + tag;

        List<DataChangeType> events = new ArrayList<>();
        DataChangeListener listener = events::add;

        try {
            auth.login("admin", "admin123");
            DataChangePublisher.getInstance().subscribe(listener);

            Book book = new Book("Observer Check " + tag, "Tester", null, null, 1);
            books.addBook(book);
            check("BOOKS event published after addBook",
                    events.contains(DataChangeType.BOOKS));

            DataChangePublisher.getInstance().unsubscribe(listener);
            events.clear();
            books.deleteBook(book.getId());
            check("no event after unsubscribe", events.isEmpty());

            auth.registerStudent(username, "secret1", null);
            auth.logout();
            check("registered student can log in",
                    auth.login(username, "secret1").getRole() == Role.STUDENT);
            expectError("duplicate username rejected",
                    () -> auth.registerStudent(username, "secret1", null));
            expectError("short password rejected",
                    () -> auth.registerStudent("another" + tag, "123", null));

            System.out.println("\nALL EXTRA CHECKS PASSED!");

        } finally {
            DataChangePublisher.getInstance().unsubscribe(listener);
            Transactions.write(em -> {
                em.createQuery("DELETE FROM User u WHERE u.username = :n")
                        .setParameter("n", username)
                        .executeUpdate();
                return null;
            });
            DatabaseManager.getInstance().shutdown();
        }
    }
}
