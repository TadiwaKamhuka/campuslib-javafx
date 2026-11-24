package com.team.campuslib;

import com.team.campuslib.model.Book;
import com.team.campuslib.model.Member;
import com.team.campuslib.service.BookService;
import com.team.campuslib.service.MemberService;
import com.team.campuslib.util.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class ServiceChecks {

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
        BookService books = new BookService();
        com.team.campuslib.util.SeedDataInitializer.seedIfEmpty();
        new com.team.campuslib.service.AuthService().login("admin", "admin123");
        MemberService members = new MemberService();
        String tag = String.valueOf(System.nanoTime());
        List<Long> bookIds = new ArrayList<>();
        Long memberId = null;

        try {
            Member m = new Member("Check Person",
                    "check" + tag + "@example.com", "0630000000");
            members.addMember(m);
            memberId = m.getId();
            final Long id = memberId;
            check("member added", id != null);

            expectError("duplicate email rejected", () ->
                    members.addMember(new Member("Other",
                            m.getEmail().toUpperCase(), null)));
            expectError("invalid email rejected", () ->
                    members.addMember(new Member("Bad", "not-an-email", null)));
            check("member search by name",
                    !members.searchMembers("check person").isEmpty());

            Member loaded = members.getMemberById(id).orElseThrow();
            loaded.setPhone("0631111111");
            members.updateMember(loaded);
            check("member update persisted", "0631111111".equals(
                    members.getMemberById(id).orElseThrow().getPhone()));

            members.deactivateMember(id);
            check("member deactivated",
                    !members.getMemberById(id).orElseThrow().isActive());
            members.activateMember(id);
            check("member reactivated",
                    members.getMemberById(id).orElseThrow().isActive());

            Book b1 = new Book("Check A " + tag, "Tester", "", "Testing", 2);
            Book b2 = new Book("Check B " + tag, "Tester", "  ", "Testing", 2);
            books.addBook(b1);
            bookIds.add(b1.getId());
            books.addBook(b2);
            bookIds.add(b2.getId());
            check("two books with blank ISBN allowed",
                    b1.getId() != null && b2.getId() != null);

            Book b3 = new Book("Check C " + tag, "Tester",
                    "ISBN-" + tag, "Testing", 2);
            books.addBook(b3);
            bookIds.add(b3.getId());
            expectError("duplicate ISBN rejected", () ->
                    books.addBook(new Book("Check D", "Tester",
                            "isbn-" + tag, "Testing", 1)));

            Book loadedBook = books.getBookById(b3.getId()).orElseThrow();
            loadedBook.setTotalCopies(5);
            books.updateBook(loadedBook);
            check("raising total copies raises available",
                    books.getBookById(b3.getId()).orElseThrow()
                            .getAvailableCopies() == 5);

            members.deleteMember(id);
            check("member without loans deleted",
                    members.getMemberById(id).isEmpty());
            memberId = null;

            System.out.println("\nALL SERVICE CHECKS PASSED!");

        } finally {
            for (Long bookId : bookIds) {
                try {
                    books.deleteBook(bookId);
                } catch (RuntimeException ignored) {
                    // Already deleted or never created.
                }
            }
            if (memberId != null) {
                try {
                    members.deleteMember(memberId);
                } catch (RuntimeException ignored) {
                    // Already deleted.
                }
            }
            DatabaseManager.getInstance().shutdown();
        }
    }
}
