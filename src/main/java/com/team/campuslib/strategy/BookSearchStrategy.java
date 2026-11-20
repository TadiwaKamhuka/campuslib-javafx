
package com.team.campuslib.strategy;

public interface BookSearchStrategy {

    String getSearchQuery();
    
    static BookSearchStrategy from(String searchType) {
        if (searchType == null) {
            throw new IllegalArgumentException(
                    "Search type cannot be null."
            );
        }

        return switch (searchType.toLowerCase()) {
            case "title" -> () ->
                    "SELECT b FROM Book b WHERE LOWER(b.title) LIKE :query";

            case "author" -> () ->
                    "SELECT b FROM Book b WHERE LOWER(b.author) LIKE :query";

            case "isbn" -> () ->
                    "SELECT b FROM Book b WHERE LOWER(b.isbn) LIKE :query";

            case "category" -> () ->
                    "SELECT b FROM Book b WHERE LOWER(b.category) LIKE :query";

            default -> throw new IllegalArgumentException(
                    "Invalid search type: " + searchType
            );
        };
    }
}
