
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
}
