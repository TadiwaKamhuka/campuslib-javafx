package com.team.campuslib.util;

import com.team.campuslib.model.Book;
import com.team.campuslib.model.Member;
import com.team.campuslib.model.Role;
import com.team.campuslib.model.User;
import com.team.campuslib.repository.BookRepository;
import com.team.campuslib.repository.MemberRepository;
import com.team.campuslib.repository.RepositoryFactory;
import com.team.campuslib.repository.UserRepository;

import org.mindrot.jbcrypt.BCrypt;

public final class SeedDataInitializer {

    private SeedDataInitializer() {
    }

    public static void seedIfEmpty() {
        boolean seeded = Transactions.write(em -> {
            UserRepository users = RepositoryFactory.createUserRepository(em);

            if (users.count() > 0) {
                return false;
            }

            BookRepository books = RepositoryFactory.createBookRepository(em);
            MemberRepository members =
                    RepositoryFactory.createMemberRepository(em);

            Member anna = new Member(
                    "Anna Kovacs", "anna.kovacs@example.com", "0630000001");
            members.save(anna);
            members.save(new Member(
                    "Peter Nagy", "peter.nagy@example.com", "0630000002"));
            members.save(new Member(
                    "Eva Toth", "eva.toth@example.com", "0630000003"));
            members.save(new Member(
                    "Daniel Szabo", "daniel.szabo@example.com", "0630000004"));

            books.save(new Book("Clean Code", "Robert C. Martin",
                    "9780132350884", "Programming", 3));
            books.save(new Book("Effective Java", "Joshua Bloch",
                    "9780134685991", "Programming", 2));
            books.save(new Book("Computer Networks", "Andrew Tanenbaum",
                    "9780132126953", "Networking", 2));
            books.save(new Book("Operating System Concepts", "Abraham Silberschatz",
                    "9781119800361", "Systems", 1));
            books.save(new Book("Database System Concepts", "Abraham Silberschatz",
                    "9780078022159", "Databases", 2));
            books.save(new Book("Introduction to Algorithms", "Thomas Cormen",
                    "9780262046305", "Algorithms", 2));

            users.save(new User("admin",
                    BCrypt.hashpw("admin123", BCrypt.gensalt()), Role.ADMIN));

            User student = new User("student",
                    BCrypt.hashpw("student123", BCrypt.gensalt()), Role.STUDENT);
            student.setMemberId(anna.getId());
            users.save(student);

            return true;
        });

        if (seeded) {
            System.out.println("Seed data created.");
        }
    }
}
