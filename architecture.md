# CampusLib — Architecture & Design Patterns

This document outlines the software architecture and design patterns used in the CampusLib JavaFX application.

---

## 1. Architectural Pattern: MVC (Model-View-Controller)

The application strictly separates concerns into three layers:
* **View (`.fxml` files & UI components):** Handles the user interface layout and presentation.
* **Controller (`controller/` package):** Handles user interactions, button clicks, and UI updates. Controllers do *not* contain database logic or SQL queries.
* **Model (`model/` package):** Contains the JPA entity classes (`User`, `Book`, `Member`, `Loan`) representing the database tables.

---

## 2. Service & Repository Layers

* **Service Layer (`service/`):** Contains the core business rules and logic. Controllers call services to perform actions.
* **Repository Layer (`repository/`):** Handles database access using Hibernate ORM and JPA. Services call repositories to fetch or save data.

---

## 3. Implemented Design Patterns

* **Singleton Pattern:** Used in `DatabaseManager` and `SessionManager` to ensure only one instance exists throughout the application lifecycle.
* **Factory Pattern:** Used in `RepositoryFactory` and `DialogFactory` to manage object creation and standard UI dialogs.
* **Strategy Pattern:** Used for flexible book searching algorithms.
* **Observer Pattern:** Implemented via `DataChangePublisher` and `DataChangeListener` so that tables and dashboard statistics automatically refresh when data changes.