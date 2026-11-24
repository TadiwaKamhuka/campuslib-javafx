package com.team.campuslib.service;

import com.team.campuslib.model.Role;
import com.team.campuslib.model.User;
import com.team.campuslib.repository.RepositoryFactory;
import com.team.campuslib.repository.UserRepository;
import com.team.campuslib.util.SessionManager;
import com.team.campuslib.util.Transactions;
import com.team.campuslib.util.ValidationHelper;

import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    public User login(String username, String password) {
        String error = ValidationHelper.validateLogin(username, password);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        User user = Transactions.read(em ->
                RepositoryFactory.createUserRepository(em)
                        .findByUsername(username.trim())
                        .orElse(null));

        if (user == null
                || !user.isActive()
                || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        SessionManager.getInstance().login(user);
        return user;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    public void registerStudent(String username, String password, Long memberId) {
        String name = username == null ? "" : username.trim();

        if (name.length() < 3) {
            throw new IllegalArgumentException(
                    "Username must be at least 3 characters.");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must be at least 6 characters.");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        Transactions.write(em -> {
            UserRepository users = RepositoryFactory.createUserRepository(em);

            if (users.findByUsername(name).isPresent()) {
                throw new IllegalArgumentException(
                        "This username is already taken.");
            }

            if (memberId != null && RepositoryFactory
                    .createMemberRepository(em).findById(memberId).isEmpty()) {
                throw new IllegalArgumentException(
                        "Member not found with ID: " + memberId);
            }

            User user = new User(name, hash, Role.STUDENT);
            user.setMemberId(memberId);
            users.save(user);
            return null;
        });
    }
}
