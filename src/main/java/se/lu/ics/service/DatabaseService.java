package se.lu.ics.service;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Skapar (en gång) SQLite-schemat och exponerar en singleton-DataSource.
 */
public final class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:vikingexpress.db";
    private static final SQLiteDataSource DS = new SQLiteDataSource();

    static {
        DS.setUrl(DB_URL);
        try (Connection c = DS.getConnection()) {
            createTablesIfNeeded(c);
        } catch (SQLException e) {
            System.err.println("DB init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource() { return DS; }

    /* ---------- Schema ---------- */

    private static void createTablesIfNeeded(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {

            /* Rollen/befattningen */
            st.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                  id          INTEGER PRIMARY KEY AUTOINCREMENT,
                  title       TEXT NOT NULL,
                  description TEXT,
                  department  TEXT
                )""");

            /* Rekrytering – id i formatet "HR yyyy/x" */
            st.execute("""
                CREATE TABLE IF NOT EXISTS recruitments (
                  id                    TEXT PRIMARY KEY,
                  role_id               INTEGER NOT NULL,
                  application_deadline  TEXT NOT NULL,
                  posting_date          TEXT NOT NULL,
                  offer_acceptance_date TEXT,
                  status                TEXT NOT NULL,
                  FOREIGN KEY (role_id) REFERENCES roles(id)
                )""");

            /* Kandidat/Applicant */
            st.execute("""
                CREATE TABLE IF NOT EXISTS applicants (
                  id               TEXT PRIMARY KEY,
                  first_name       TEXT NOT NULL,
                  last_name        TEXT NOT NULL,
                  email            TEXT NOT NULL,
                  phone            TEXT,
                  application_date TEXT NOT NULL,
                  rank             INTEGER DEFAULT 0
                )""");

            /* M:N tabell - vilken kandidat har sökt vilken rekrytering */
            st.execute("""
                CREATE TABLE IF NOT EXISTS applications (
                  applicant_id   TEXT NOT NULL,
                  recruitment_id TEXT NOT NULL,
                  application_date TEXT NOT NULL,
                  PRIMARY KEY (applicant_id, recruitment_id),
                  FOREIGN KEY (applicant_id)   REFERENCES applicants(id),
                  FOREIGN KEY (recruitment_id) REFERENCES recruitments(id)
                )""");

            /* Intervjuer */
            st.execute("""
                CREATE TABLE IF NOT EXISTS interviews (
                  id            TEXT PRIMARY KEY,
                  recruitment_id TEXT NOT NULL,
                  applicant_id   TEXT NOT NULL,
                  date_time      TEXT NOT NULL,
                  location       TEXT,
                  interviewer    TEXT,
                  status         TEXT NOT NULL,
                  notes          TEXT,
                  FOREIGN KEY (recruitment_id) REFERENCES recruitments(id),
                  FOREIGN KEY (applicant_id)   REFERENCES applicants(id)
                )""");
        }
    }

    private DatabaseService() {}   // Förhindra instansiering
}
