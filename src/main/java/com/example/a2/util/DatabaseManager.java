package com.example.a2.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton DatabaseManager - manages SQLite database connection
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private final Connection connection;
    private static final String DB_URL = "jdbc:sqlite:budget.db";

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    public Connection getConnection() {
        return connection;
    }

    private void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
                UNIQUE(name, type)
            )
        """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                amount REAL NOT NULL CHECK (amount > 0),
                type TEXT NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
                category_id INTEGER NOT NULL,
                transaction_date TEXT NOT NULL,
                description TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
            )
        """;

        String createBudgetsTable = """
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                category_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                month INTEGER NOT NULL,
                year INTEGER NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
                UNIQUE(user_id, category_id, month, year)
            )
        """;

        String createFinancialGoalsTable = """
            CREATE TABLE IF NOT EXISTS financial_goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                target_amount REAL NOT NULL CHECK (target_amount > 0),
                saved_amount REAL NOT NULL DEFAULT 0 CHECK (saved_amount >= 0),
                deadline TEXT NOT NULL,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;

        String createGoalContributionsTable = """
            CREATE TABLE IF NOT EXISTS goal_contributions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                goal_id INTEGER NOT NULL,
                amount REAL NOT NULL CHECK (amount > 0),
                transaction_id INTEGER,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (goal_id) REFERENCES financial_goals(id) ON DELETE CASCADE,
                FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE SET NULL
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCategoriesTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createBudgetsTable);
            stmt.execute(createFinancialGoalsTable);
            stmt.execute(createGoalContributionsTable);
            stmt.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS idx_goal_contrib_transaction_unique
                ON goal_contributions(transaction_id)
                WHERE transaction_id IS NOT NULL
                """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_financial_goals_user ON financial_goals(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_goal_contributions_goal ON goal_contributions(goal_id)");
            runSchemaMigrations(stmt);

            // Insert default categories
            stmt.execute("""
                INSERT OR IGNORE INTO categories (name, type) VALUES
                ('Salary', 'INCOME'),
                ('Freelance', 'INCOME'),
                ('Investment', 'INCOME'),
                ('Food', 'EXPENSE'),
                ('Transport', 'EXPENSE'),
                ('Entertainment', 'EXPENSE'),
                ('Utilities', 'EXPENSE'),
                ('Healthcare', 'EXPENSE'),
                ('Shopping', 'EXPENSE')
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(category_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_budgets_user ON budgets(user_id)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void runSchemaMigrations(Statement stmt) throws SQLException {
        migrateUsersTable(stmt);
        migrateTransactionsTable(stmt);
        migrateBudgetsTable(stmt);
    }

    private void migrateUsersTable(Statement stmt) throws SQLException {
        if (!columnExists("users", "email")) {
            stmt.execute("ALTER TABLE users ADD COLUMN email TEXT");
            if (columnExists("users", "username")) {
                stmt.execute("UPDATE users SET email = username WHERE email IS NULL");
            }
        }

        if (columnExists("users", "username") || !isUsersIdPrimaryKey()) {
            stmt.execute("PRAGMA foreign_keys = OFF");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);
            if (columnExists("users", "username")) {
                stmt.execute("""
                    INSERT OR REPLACE INTO users_new (email, password_hash, created_at)
                    SELECT COALESCE(NULLIF(email, ''), username),
                           password_hash,
                           COALESCE(created_at, CURRENT_TIMESTAMP)
                    FROM users
                    WHERE COALESCE(NULLIF(email, ''), username) IS NOT NULL
                """);
            } else {
                stmt.execute("""
                    INSERT OR REPLACE INTO users_new (email, password_hash, created_at)
                    SELECT email,
                           password_hash,
                           COALESCE(created_at, CURRENT_TIMESTAMP)
                    FROM users
                    WHERE email IS NOT NULL
                      AND trim(email) <> ''
                      AND lower(trim(email)) NOT LIKE 'id integer primary key%'
                """);
            }
            stmt.execute("DROP TABLE users");
            stmt.execute("ALTER TABLE users_new RENAME TO users");
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique ON users(email)");
    }

    private void migrateTransactionsTable(Statement stmt) throws SQLException {
        if (!columnExists("transactions", "transaction_date")) {
            stmt.execute("ALTER TABLE transactions ADD COLUMN transaction_date TEXT");
            if (columnExists("transactions", "year") && columnExists("transactions", "month")) {
                stmt.execute("""
                    UPDATE transactions
                    SET transaction_date = printf('%04d-%02d-01', year, month)
                    WHERE transaction_date IS NULL
                """);
            }
        }
    }

    private void migrateBudgetsTable(Statement stmt) throws SQLException {
        if (!columnExists("budgets", "month")) {
            stmt.execute("ALTER TABLE budgets ADD COLUMN month INTEGER");
        }
        if (!columnExists("budgets", "year")) {
            stmt.execute("ALTER TABLE budgets ADD COLUMN year INTEGER");
        }

        if (columnExists("budgets", "start_date")) {
            stmt.execute("""
                UPDATE budgets
                SET month = CAST(substr(start_date, 6, 2) AS INTEGER)
                WHERE month IS NULL
            """);
            stmt.execute("""
                UPDATE budgets
                SET year = CAST(substr(start_date, 1, 4) AS INTEGER)
                WHERE year IS NULL
            """);
        }
    }

    private boolean isUsersIdPrimaryKey() throws SQLException {
        String sql = "PRAGMA table_info(users)";
        try (Statement pragmaStmt = connection.createStatement();
             java.sql.ResultSet rs = pragmaStmt.executeQuery(sql)) {
            while (rs.next()) {
                if ("id".equalsIgnoreCase(rs.getString("name"))) {
                    return rs.getInt("pk") == 1;
                }
            }
            return false;
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement pragmaStmt = connection.createStatement();
             java.sql.ResultSet rs = pragmaStmt.executeQuery(sql)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
