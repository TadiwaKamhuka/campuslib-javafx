package com.team.campuslib;

import com.team.campuslib.dto.LoanRow;
import com.team.campuslib.model.Book;
import com.team.campuslib.model.Loan;
import com.team.campuslib.model.LoanStatus;
import com.team.campuslib.model.Member;
import com.team.campuslib.model.User;
import com.team.campuslib.service.AuthService;
import com.team.campuslib.service.BookService;
import com.team.campuslib.service.DashboardService;
import com.team.campuslib.service.LoanService;
import com.team.campuslib.service.MemberService;
import com.team.campuslib.util.DatabaseManager;
import com.team.campuslib.util.SeedDataInitializer;
import com.team.campuslib.util.SessionManager;
import com.team.campuslib.util.Transactions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanChecks {

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

    private static int available(BookService books, Long id) {
        return books.getBookById(id).orElseThrow().getAvailableCopies();
    }

    public static void main(String[] args) {
        SeedDataInitializer.seedIfEmpty();

        AuthService auth = new AuthService();
        BookService books = new BookService();
        MemberService members = new MemberService();
        LoanService loans = new LoanService();
        DashboardService dashboard = new DashboardService();

        String tag = String.valueOf(System.nanoTime());
        List<Long> bookIds = new ArrayList<>();
        List<Long> memberIds = new ArrayList<>();

        try {
            expectError("wrong password rejected",
                    () -> auth.login("admin", "wrong"));
            User admin = auth.login("admin", "admin123");
            check("admin login ok and hash is BCrypt",
                    admin.getPasswordHash().startsWith("$2")
                            && !admin.getPasswordHash().equals("admin123"));

            Book book = new Book("Loan Check " + tag, "Tester",
                    "LC-" + tag, "Testing", 2);
            books.addBook(book);
            final Long bid = book.getId();
            bookIds.add(bid);

            Member a = new Member("Loan A " + tag, "a" + tag + "@example.com", null);
            Member b = new Member("Loan B " + tag, "b" + tag + "@example.com", null);
            Member c = new Member("Loan C " + tag, "c" + tag + "@example.com", null);
            members.addMember(a);
            members.addMember(b);
            members.addMember(c);
            final Long aId = a.getId();
            final Long bId = b.getId();
            final Long cId = c.getId();
            memberIds.add(aId);
            memberIds.add(bId);
            memberIds.add(cId);

            final LoanRow loanA = loans.borrowBook(bid, aId);
            check("borrow ok and due date is 14 days later",
                    loanA.getDueDate().equals(LocalDate.now().plusDays(14)));
            check("available copies decreased", available(books, bid) == 1);

            expectError("duplicate active loan rejected",
                    () -> loans.borrowBook(bid, aId));

            final LoanRow loanB = loans.borrowBook(bid, bId);
            check("available copies is now 0", available(books, bid) == 0);
            expectError("unavailable book rejected",
                    () -> loans.borrowBook(bid, cId));
            expectError("member with active loan cannot be deleted",
                    () -> members.deleteMember(aId));
            expectError("book with loans cannot be deleted",
                    () -> books.deleteBook(bid));

            LoanRow returned = loans.returnBook(loanA.getId());
            check("return sets status RETURNED",
                    returned.getStatus() == LoanStatus.RETURNED
                            && returned.getReturnDate() != null);
            check("available copies increased", available(books, bid) == 1);
            expectError("double return rejected",
                    () -> loans.returnBook(loanA.getId()));

            members.deactivateMember(cId);
            expectError("inactive member cannot borrow",
                    () -> loans.borrowBook(bid, cId));

            Transactions.write(em -> {
                em.find(Loan.class, loanB.getId())
                        .setDueDate(LocalDate.now().minusDays(1));
                return null;
            });
            check("overdue loan listed", loans.getOverdueLoans().stream()
                    .anyMatch(l -> l.getId().equals(loanB.getId())));
            check("overdue status stored and flagged",
                    loans.getAllLoans().stream()
                            .anyMatch(l -> l.getId().equals(loanB.getId())
                                    && l.getStatus() == LoanStatus.OVERDUE
                                    && l.isOverdue()));
            check("dashboard counts overdue loans",
                    dashboard.getStats().getOverdueLoans() >= 1);
            loans.returnBook(loanB.getId());

            auth.logout();
            auth.login("student", "student123");
            expectError("student cannot add book", () ->
                    books.addBook(new Book("X", "Y", null, null, 1)));
            expectError("student cannot return a loan",
                    () -> loans.returnBook(loanB.getId()));
            expectError("student cannot borrow for someone else",
                    () -> loans.borrowBook(bid, bId));
            expectError("student cannot list members",
                    () -> members.getAllMembers());

            Long ownMember = SessionManager.getInstance()
                    .getCurrentUser().getMemberId();
            LoanRow own = loans.borrowBook(bid, ownMember);
            check("student borrowed for own member",
                    own.getMemberId().equals(ownMember));
            check("student sees own loans",
                    !loans.getLoansByMember(ownMember).isEmpty());

            auth.logout();
            auth.login("admin", "admin123");
            loans.returnBook(own.getId());

            System.out.println("\nALL LOAN CHECKS PASSED!");

        } finally {
            try {
                Transactions.write(em -> {
                    for (Long id : bookIds) {
                        em.createQuery("DELETE FROM Loan l WHERE l.book.id = :id")
                                .setParameter("id", id)
                                .executeUpdate();
                    }
                    return null;
                });
                auth.logout();
                auth.login("admin", "admin123");
                for (Long id : bookIds) {
                    try {
                        books.deleteBook(id);
                    } catch (RuntimeException ignored) {
                        // Already deleted.
                    }
                }
                for (Long id : memberIds) {
                    try {
                        members.deleteMember(id);
                    } catch (RuntimeException ignored) {
                        // Already deleted.
                    }
                }
            } catch (RuntimeException ignored) {
                // Cleanup is best-effort.
            }
            DatabaseManager.getInstance().shutdown();
        }
    }
}
