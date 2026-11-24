package com.team.campuslib.dto;

public class DashboardStats {

    private final long totalBooks;
    private final long totalMembers;
    private final long activeLoans;
    private final long overdueLoans;

    public DashboardStats(long totalBooks, long totalMembers,
                          long activeLoans, long overdueLoans) {
        this.totalBooks = totalBooks;
        this.totalMembers = totalMembers;
        this.activeLoans = activeLoans;
        this.overdueLoans = overdueLoans;
    }

    public long getTotalBooks() { return totalBooks; }
    public long getTotalMembers() { return totalMembers; }
    public long getActiveLoans() { return activeLoans; }
    public long getOverdueLoans() { return overdueLoans; }
}
