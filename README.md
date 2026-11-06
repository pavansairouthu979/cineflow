# CineFlow — Multiplex Movie Ticket Reservation System

CineFlow is a production-grade desktop application for multiplex movie ticket management built using **Java 21**, **JavaFX 21**, **Maven**, and **MySQL 8.x** via pure **JDBC**.

This system implements a full ticketing workflow including user authentication with salted SHA-256 password hashing, movie catalog browsing with live search, real-time visual seat map selection, 5-minute temporary seat holds with countdown timers, concurrency-safe ACID booking transactions with `SELECT ... FOR UPDATE`, ticket summary generation, booking cancellation with refunds, administrative management (movies, auditoriums, showtimes, bookings), revenue reports with JavaFX charts, and CSV data export.

---

## Technology Stack

| Component | Technology | Description |
|---|---|---|
| Language | Java 21 (LTS) | Modern Java with records, pattern matching, try-with-resources |
| UI Framework | JavaFX 21.x | Desktop GUI built in pure Java code (No FXML) |
| Build Tool | Apache Maven | Dependency management and build automation |
| Database | MySQL 8.x | Relational DBMS using InnoDB engine for ACID compliance |
| Persistence | JDBC (java.sql) | Native PreparedStatement queries and Connection transactions |
| UI Styling | JavaFX CSS | Cinematic dark navy palette (`src/main/resources/css/styles.css`) |
| Password Security | JDK SecureRandom & MessageDigest | 16-byte cryptographically secure salt + SHA-256 hashing |

> **Strict Architectural Boundary**: CineFlow is a standalone desktop application. It intentionally uses no heavyweight frameworks or ORMs (no Spring Boot, Hibernate, JPA, Lombok, or web wrappers) to demonstrate mastery of core Java, JDBC, and object-oriented principles.

---

## Layered Architecture

CineFlow strictly follows a 5-layer separation of concerns:

```
[ JavaFX UI Layer ]        (Pure Java controls, event handlers, CSS styling)
        │
[ Service Layer ]          (Business rules, validation, transaction coordination)
        │
[ DAO Layer ]              (Every SQL query strictly encapsulated with PreparedStatement)
        │
[ JDBC / Model Layer ]     (Connection management, POJO domain models)
        │
[ MySQL 8.x Database ]     (InnoDB tables, Foreign Keys, Unique constraints, Indexes)
```

### Package Structure

```
com.cineflow
├── Main.java              # Application launcher and Phase 1 verification
├── model/                 # Entity POJOs (User, Movie, Screen, Seat, Showtime, Booking, Payment)
├── dao/                   # DAO classes (UserDAO, MovieDAO, ScreenDAO, ShowtimeDAO, BookingDAO)
├── service/               # Service classes (AuthService, MovieService, BookingService, ReportService)
├── ui/
│   ├── common/            # Reusable UI widgets, alerts, navigation bar
│   ├── auth/              # LoginView and RegistrationView
│   ├── admin/             # AdminDashboard, MovieView, ScreenView, ShowtimeView, ReportView
│   └── customer/          # CustomerDashboard, MovieBrowser, SeatMapView, PaymentDialog, TicketView
└── util/                  # DBConnection, PasswordUtil, AlertHelper, CsvExporter
```

---

## Database Design (`cineflow_db`)

The database consists of **9 normalized InnoDB tables**:

1. **`users`**: User credentials (`id`, `username`, `password_hash`, `salt`, `full_name`, `email`, `role` [ADMIN/CUSTOMER], `active`).
2. **`movies`**: Catalog (`id`, `title`, `genre`, `language`, `duration_minutes`, `rating`, `description`, `release_date`, `active`).
3. **`screens`**: Auditoriums (`id`, `name`, `total_rows`, `seats_per_row`).
4. **`seats`**: Screen seating layout (`id`, `screen_id`, `row_label`, `seat_number`, `seat_type` [REGULAR/PREMIUM]).
   - *Unique Constraint*: `(screen_id, row_label, seat_number)` prevents duplicate seat configurations.
5. **`showtimes`**: Screenings (`id`, `movie_id`, `screen_id`, `start_time`, `price_regular`, `price_premium`).
6. **`bookings`**: Customer reservations (`id`, `user_id`, `showtime_id`, `booking_time`, `total_amount`, `status` [CONFIRMED/CANCELLED]).
7. **`booking_seats`**: Individual seats reserved in a booking (`id`, `booking_id`, `showtime_id`, `seat_id`, `price`).
   - *Unique Constraint*: `(showtime_id, seat_id)` prevents double bookings at the database level.
8. **`seat_holds`**: 5-minute checkout holds (`id`, `showtime_id`, `seat_id`, `user_id`, `held_until`).
   - *Unique Constraint*: `(showtime_id, seat_id)` prevents concurrent users from holding the same seat.
9. **`payments`**: Payment records (`id`, `booking_id`, `method` [CASH/UPI/CARD], `amount`, `status` [PAID/REFUNDED], `paid_at`).

---

## Sample Credentials for Evaluation

The seed script initializes three deterministic accounts hashed using salted SHA-256:

| Role | Username | Password | Full Name | Email |
|---|---|---|---|---|
| **ADMIN** | `admin` | `Admin@123` | System Administrator | admin@cineflow.com |
| **CUSTOMER** | `john_doe` | `Customer@123` | John Doe | john.doe@example.com |
| **CUSTOMER** | `jane_smith` | `Customer@123` | Jane Smith | jane.smith@example.com |

---

## Setup & Running Guide

### 1. Prerequisites
- **JDK 21** or higher installed and on `PATH`.
- **Apache Maven 3.9+** installed and on `PATH`.
- **MySQL Server 8.0+** running locally on port 3306.

### 2. Database Initialization
Execute the schema and seed scripts in MySQL:
```bash
# Using MySQL Command Line Client
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed_data.sql
```

### 3. Database Credentials Configuration
Copy `db.properties.example` to `db.properties` in the project root:
```properties
db.url=jdbc:mysql://localhost:3306/cineflow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=your_mysql_password
```
*(Note: `db.properties` is strictly ignored by `.gitignore` to prevent credential leaks).*

### 4. Build and Run
Compile the project:
```bash
mvn clean compile
```

Launch the JavaFX desktop application:
```bash
mvn javafx:run
```

---

## Development Roadmap (8 Strict Phases)

- [x] **Phase 1: Project Foundation** — Maven configuration, package structure, database schema, deterministic seed data, CSS design system, JDBC connection verification window.
- [x] **Phase 2: Authentication** — User entity, UserDAO, salted SHA-256 password hashing, AuthService, Login & Registration JavaFX views, role-based navigation.
- [ ] **Phase 3: Admin Management** — Movie CRUD, Screen configuration, dynamic seat layout generation, Showtime scheduling with conflict/overlap detection.
- [ ] **Phase 4: Customer Browsing & Seat Map** — Customer dashboard, search-as-you-type, multi-attribute filtering, interactive JavaFX seat map with price calculation.
- [ ] **Phase 5: Seat Holds & ACID Booking Transaction** — 5-minute hold timer with Timeline countdown, simulated payment dialog, JDBC transaction with `SELECT ... FOR UPDATE`, ticket summary.
- [ ] **Phase 6: Booking Management** — Customer booking history, ticket viewing, cancellation before showtime with refund transaction, admin booking oversight with SQL JOIN filters.
- [ ] **Phase 7: Reports & CSV Export** — Total revenue, today's bookings, 7-day revenue BarChart, top movies PieChart, showtime occupancy metrics, RFC-4180 CSV export.
- [ ] **Phase 8: Final Quality Review** — End-to-end integration testing, input validation audits, error handling polish, viva defense preparation.

---

## Team Contributions (5 Team Members)

- **Member 1**: Authentication, Registration, Login, Salted Password Hashing, UserDAO.
- **Member 2**: Movie Management, Screen Layout Generation, Showtime Overlap Detection.
- **Member 3**: Customer Dashboard, Movie Catalog Browser, Search/Filters, Interactive Seat Map.
- **Member 4**: Seat Holds, Timeline Countdown, Payment Dialog, ACID Booking Transaction, Cancellation.
- **Member 5**: Admin Booking JOIN Queries, Revenue Reports, JavaFX Charts, CSV Exporter.

