
package com.team.campuslib.util;

import com.team.campuslib.model.Book;

public class ValidationHelper {

    public static String validateLogin(String username, String password) {
        if (username == null || username.isBlank()) {
            return "Username is required.";
        }

        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }

        return null;
    }

    public static String validateBook(Book book) {
        if (book == null) {
            return "Book cannot be null.";
        }

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            return "Book title is required.";
        }

        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            return "Book author is required.";
        }

        if (book.getTotalCopies() <= 0) {
            return "Total copies must be greater than zero.";
        }

        if (book.getAvailableCopies() < 0
                || book.getAvailableCopies() > book.getTotalCopies()) {
            return "Available copies must be between zero and total copies.";
        }

        return null;
    }

    public static String validateSearch(String searchType, String query) {
        if (searchType == null || searchType.isBlank()) {
            return "Search type is required.";
        }

        if (query == null || query.isBlank()) {
            return "Search query is required.";
        }

        switch (searchType.toLowerCase()) {
            case "title":
            case "author":
            case "isbn":
            case "category":
                return null;

            default:
                return "Invalid search type.";
        }
    }

    public static String validateBookId(Long id) {
        if (id == null || id <= 0) {
            return "A valid book ID is required.";
        }

        return null;
    }

    public static String validateMember(com.team.campuslib.model.Member member) {
        if (member == null) {
            return "Member cannot be null.";
        }

        if (member.getFullName() == null || member.getFullName().isBlank()) {
            return "Member full name is required.";
        }

        if (member.getEmail() == null || member.getEmail().isBlank()) {
            return "Member email is required.";
        }

        if (!member.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Member email format is invalid.";
        }

        if (member.getPhone() != null
                && !member.getPhone().matches("^[0-9+()\\-\\s]{6,20}$")) {
            return "Member phone number is invalid.";
        }

        return null;
    }

    public static String validateMemberId(Long id) {
        if (id == null || id <= 0) {
            return "A valid member ID is required.";
        }

        return null;
    }
}
