package com.team.campuslib.service;

import com.team.campuslib.dto.LoanRow;
import com.team.campuslib.model.Book;
import com.team.campuslib.model.Loan;
import com.team.campuslib.model.LoanStatus;
import com.team.campuslib.model.Member;
import com.team.campuslib.observer.DataChangePublisher;
import com.team.campuslib.observer.DataChangeType;
import com.team.campuslib.repository.BookRepository;
import com.team.campuslib.repository.LoanRepository;
import com.team.campuslib.repository.MemberRepository;
import com.team.campuslib.repository.RepositoryFactory;
import com.team.campuslib.util.SessionManager;
import com.team.campuslib.util.Transactions;
import com.team.campuslib.util.ValidationHelper;

import java.time.LocalDate;
import java.util.List;

public class LoanService {

    public static final int LOAN_DAYS = 14;
    public static final int MAX_ACTIVE_LOANS = 5;

    public LoanRow borrowBook(Long bookId, Long memberId) {
        SessionManager session = SessionManager.getInstance();
        session.requireLoggedIn();

        requireValid(ValidationHelper.validateBookId(bookId));
        requireValid(ValidationHelper.validateMemberId(memberId));

        if (!session.isAdmin()) {
            Long ownMemberId = session.getCurrentUser().getMemberId();

            if (ownMemberId == null) {
                throw new IllegalStateException(
                        "Your account is not linked to a library member.");
            }

            if (!ownMemberId.equals(memberId)) {
                throw new IllegalStateException(
                        "Students can only borrow books for themselves.");
            }
        }

        LoanRow row = Transactions.write(em -> {
            BookRepository books = RepositoryFactory.createBookRepository(em);
            MemberRepository members =
                    RepositoryFactory.createMemberRepository(em);
            LoanRepository loans = RepositoryFactory.createLoanRepository(em);

            Book book = books.findById(bookId).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Book not found with ID: " + bookId));

            Member member = members.findById(memberId).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Member not found with ID: " + memberId));

            if (!member.isActive()) {
                throw new IllegalStateException(
                        "Inactive members cannot borrow books.");
            }

            if (book.getAvailableCopies() <= 0) {
                throw new IllegalStateException(
                        "No copies of this book are currently available.");
            }

            if (loans.existsOpenLoan(bookId, memberId)) {
                throw new IllegalStateException(
                        "This member already has an active loan for this book.");
            }

            if (loans.countOpenByMember(memberId) >= MAX_ACTIVE_LOANS) {
                throw new IllegalStateException(
                        "This member has reached the limit of "
                                + MAX_ACTIVE_LOANS + " active loans.");
            }

            LocalDate today = LocalDate.now();
            Loan loan = new Loan(book, member, today, today.plusDays(LOAN_DAYS));

            book.setAvailableCopies(book.getAvailableCopies() - 1);
            loans.save(loan);

            return LoanRow.from(loan);
        });

        DataChangePublisher.publish(DataChangeType.BOOKS);
        DataChangePublisher.publish(DataChangeType.LOANS);
        return row;
    }

    public LoanRow returnBook(Long loanId) {
        SessionManager.getInstance().requireAdmin();

        if (loanId == null || loanId <= 0) {
            throw new IllegalArgumentException("A valid loan ID is required.");
        }

        LoanRow row = Transactions.write(em -> {
            LoanRepository loans = RepositoryFactory.createLoanRepository(em);

            Loan loan = loans.findById(loanId).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Loan not found with ID: " + loanId));

            if (loan.getReturnDate() != null
                    || loan.getStatus() == LoanStatus.RETURNED) {
                throw new IllegalStateException(
                        "This loan has already been returned.");
            }

            loan.setReturnDate(LocalDate.now());
            loan.setStatus(LoanStatus.RETURNED);

            Book book = loan.getBook();
            book.setAvailableCopies(Math.min(
                    book.getTotalCopies(), book.getAvailableCopies() + 1));

            return LoanRow.from(loan);
        });

        DataChangePublisher.publish(DataChangeType.BOOKS);
        DataChangePublisher.publish(DataChangeType.LOANS);
        return row;
    }

    public List<LoanRow> getAllLoans() {
        SessionManager.getInstance().requireAdmin();
        markOverdueLoans();

        return Transactions.read(em -> toRows(
                RepositoryFactory.createLoanRepository(em).findAll()));
    }

    public List<LoanRow> getActiveLoans() {
        SessionManager.getInstance().requireAdmin();
        markOverdueLoans();

        return Transactions.read(em -> toRows(
                RepositoryFactory.createLoanRepository(em).findOpen()));
    }

    public List<LoanRow> getOverdueLoans() {
        SessionManager.getInstance().requireAdmin();
        markOverdueLoans();

        return Transactions.read(em -> toRows(
                RepositoryFactory.createLoanRepository(em)
                        .findOverdue(LocalDate.now())));
    }

    public List<LoanRow> getLoansByMember(Long memberId) {
        SessionManager session = SessionManager.getInstance();
        session.requireLoggedIn();
        requireValid(ValidationHelper.validateMemberId(memberId));

        if (!session.isAdmin()
                && !memberId.equals(session.getCurrentUser().getMemberId())) {
            throw new IllegalStateException(
                    "You can only view your own loans.");
        }

        markOverdueLoans();

        return Transactions.read(em -> toRows(
                RepositoryFactory.createLoanRepository(em)
                        .findByMember(memberId)));
    }

    public int markOverdueLoans() {
        return Transactions.write(em ->
                RepositoryFactory.createLoanRepository(em)
                        .markOverdue(LocalDate.now()));
    }

    private static List<LoanRow> toRows(List<Loan> loans) {
        return loans.stream().map(LoanRow::from).toList();
    }

    private static void requireValid(String error) {
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
    }
}
