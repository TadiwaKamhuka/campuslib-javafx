package com.team.campuslib.service;

import com.team.campuslib.dto.DashboardStats;
import com.team.campuslib.repository.RepositoryFactory;
import com.team.campuslib.util.SessionManager;
import com.team.campuslib.util.Transactions;

import java.time.LocalDate;

public class DashboardService {

    public DashboardStats getStats() {
        SessionManager.getInstance().requireLoggedIn();

        return Transactions.read(em -> {
            LocalDate today = LocalDate.now();

            return new DashboardStats(
                    RepositoryFactory.createBookRepository(em).count(),
                    RepositoryFactory.createMemberRepository(em).count(),
                    RepositoryFactory.createLoanRepository(em).countOpen(),
                    RepositoryFactory.createLoanRepository(em)
                            .countOverdue(today));
        });
    }
}
