package se.lu.ics.service;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Skapar (en gång) SQLite-schemat och exponerar en singleton-DataSource.
 */
public final class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:vikingexpress.db";
    private static final SQLiteDataSource DS = new SQLiteDataSource();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        DS.setUrl(DB_URL);
        try (Connection c = DS.getConnection()) {
            createTablesIfNeeded(c);
            ensureBasicDataExists(c);
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
    
    /**
     * Ensure some basic test data exists in the database
     * @param c Database connection
     * @throws SQLException if a database access error occurs
     */
    private static void ensureBasicDataExists(Connection c) throws SQLException {
        // First check if there are roles in the database
        int roleCount = 0;
        try (Statement stmt = c.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM roles");
            if (rs.next()) {
                roleCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error checking roles: " + e.getMessage());
        }
        
        // If no roles exist, add some sample roles
        if (roleCount == 0) {
            String[] roleTitles = {"Truck Driver", "HR Administrator", "Software Developer"};
            String[] departments = {"Operations", "Human Resources", "IT"};
            String[] descriptions = {
                "Commercial driver responsible for transporting goods across Europe.",
                "Admin support for HR department, handling recruitment and employee records.",
                "Developer with skills in Java, working on internal systems."
            };
            
            String insertRoleSql = "INSERT INTO roles (title, description, department) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = c.prepareStatement(insertRoleSql)) {
                for (int i = 0; i < roleTitles.length; i++) {
                    pstmt.setString(1, roleTitles[i]);
                    pstmt.setString(2, descriptions[i]);
                    pstmt.setString(3, departments[i]);
                    pstmt.executeUpdate();
                }
            }
            
            System.out.println("Added sample roles to database");
        }
        
        // Fix any bad date formats in interviews table
        try (Statement stmt = c.createStatement()) {
            stmt.execute("DELETE FROM interviews WHERE date_time LIKE '%1748462714000%'");
        } catch (SQLException e) {
            System.err.println("Error fixing interview dates: " + e.getMessage());
        }
    }

    private DatabaseService() {}   // Förhindra instansiering
}
