# CampusLib Backend Contract (v2)

Owner: Backend & Database Lead. Package: com.team.campuslib. Updated 23 Nov 2026.
Status: Book, Member, Loan, Auth, Observer and Dashboard are implemented and tested against SQLite. Items marked (pending approval) await the Master Overseer.

## General rules
- Services are plain classes: `new BookService()`, `new MemberService()`, `new LoanService()`, `new AuthService()`, `new DashboardService()`.
- Call `SeedDataInitializer.seedIfEmpty()` once at application start and `DatabaseManager.getInstance().shutdown()` on exit.
- The frontend receives the JPA entities `Book` and `Member` directly (pending approval). Loans arrive as `dto.LoanRow`. Never call `getLoans()` or `loan.getBook()` from the UI.
- Invalid input throws `IllegalArgumentException`. A broken business or permission rule throws `IllegalStateException`. Show `e.getMessage()` in an alert. Any other `RuntimeException` is an unexpected database error: show a generic message.
- Lists are never null. `getXById` returns `Optional`. Dates are `java.time.LocalDate`; ids are `Long`.

## Book (entity `model.Book`)
Fields: id (Long, auto), title (required), author (required), isbn (optional, unique case-insensitive, blank becomes null), category (optional), totalCopies (int, greater than 0), availableCopies (int, backend-controlled: edits are ignored).

BookService:
- `List<Book> getAllBooks()`
- `Optional<Book> getBookById(Long id)`
- `List<Book> searchBooks(String searchType, String query)`: searchType is "title", "author", "isbn" or "category" (case-insensitive); query must not be blank (use getAllBooks to list everything); contains-match, case-insensitive
- ADMIN only: `void addBook(Book)` (id is set on the passed object), `Book updateBook(Book)` (available = total minus copies on loan; total cannot drop below copies on loan), `void deleteBook(Long id)` (blocked if the book has any loan record)

## Member (entity `model.Member`)
Fields: id, fullName (required), email (required, valid format, unique case-insensitive, stored lowercase), phone (optional, 6-20 chars), active (changed only by activate/deactivate), registrationDate (set by backend).

MemberService, ADMIN only for every method:
- `getAllMembers()`, `getMemberById(Long)`, `searchMembers(String query)` (name, email or phone; blank returns all)
- `addMember(Member)`, `Member updateMember(Member)` (active and registrationDate preserved)
- `deactivateMember(Long)`, `activateMember(Long)`
- `deleteMember(Long)`: blocked if the member has any loan (use deactivate instead)

## Loans
`LoanRow` getters: getId, getBookId, getBookTitle, getMemberId, getMemberName, getBorrowDate, getDueDate, getReturnDate, getStatus (ACTIVE, RETURNED or OVERDUE), isOverdue. They work with PropertyValueFactory.

LoanService:
- `LoanRow borrowBook(Long bookId, Long memberId)`: ADMIN for any member; STUDENT only for the member linked to their account
- `LoanRow returnBook(Long loanId)`: ADMIN only
- `List<LoanRow> getAllLoans()`, `getActiveLoans()`, `getOverdueLoans()`: ADMIN only
- `List<LoanRow> getLoansByMember(Long memberId)`: ADMIN, or a student for their own member
- `int markOverdueLoans()`: called automatically by the list methods

Rules: copies greater than 0; no second unreturned loan for the same book and member; member must be active; at most 5 unreturned loans per member (pending approval); due date is borrow date plus 14 days; overdue means returnDate is null and today is after dueDate; a returned loan cannot be returned again.

## Authentication
- `User login(String username, String password)` sets the session; bad credentials throw `IllegalArgumentException("Invalid username or password.")`
- `logout()`, `getCurrentUser()`, `registerStudent(String username, String password, Long memberId)`
- `SessionManager.getInstance()`: getCurrentUser(), isLoggedIn(), isAdmin(); the role is `getCurrentUser().getRole()` (ADMIN or STUDENT)
- Passwords are BCrypt-hashed with a random salt. The student-to-member link is `User.getMemberId()` (pending approval).

## Dashboard
`DashboardStats getStats()` (logged-in users): getTotalBooks (titles), getTotalMembers, getActiveLoans (all unreturned loans), getOverdueLoans (unreturned and past due).

## Observer
- `DataChangePublisher.getInstance().subscribe(listener)` and `.unsubscribe(listener)`; `DataChangeListener.onDataChanged(DataChangeType type)`
- Types: BOOKS, MEMBERS, LOANS. Book writes publish BOOKS, member writes publish MEMBERS, borrow and return publish BOOKS and LOANS.
- Synchronous, after a successful commit. Subscribe in `initialize()`, unsubscribe when the view is replaced, and refresh the table inside the callback.

## Seed data (first run only, when the users table is empty)
admin/admin123 (ADMIN), student/student123 (STUDENT, linked to Anna Kovacs), 6 books, 4 members.
