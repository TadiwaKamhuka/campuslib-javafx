package com.team.campuslib.dto;

import com.team.campuslib.model.Loan;
import com.team.campuslib.model.LoanStatus;

import java.time.LocalDate;

public class LoanRow {

    private final Long id;
    private final Long bookId;
    private final String bookTitle;
    private final Long memberId;
    private final String memberName;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private final LocalDate returnDate;
    private final LoanStatus status;
    private final boolean overdue;

    public LoanRow(Long id, Long bookId, String bookTitle, Long memberId,
                   String memberName, LocalDate borrowDate, LocalDate dueDate,
                   LocalDate returnDate, LoanStatus status, boolean overdue) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.overdue = overdue;
    }

    public static LoanRow from(Loan loan) {
        boolean overdue = loan.getReturnDate() == null
                && LocalDate.now().isAfter(loan.getDueDate());

        return new LoanRow(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getMember().getId(),
                loan.getMember().getFullName(),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus(),
                overdue);
    }

    public Long getId() { return id; }
    public Long getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LoanStatus getStatus() { return status; }
    public boolean isOverdue() { return overdue; }
}
