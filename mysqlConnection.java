import java.sql.*;
import java.util.*;

public class mysqlConnection {

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Driver definition succeed");
        } catch (Exception ex) {
            System.out.println("Driver definition failed");
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/test?useSSL=false&serverTimezone=IST", "root", "Aa123456")) {

            System.out.println("SQL connection succeed");

            // 1. עדכון טיסה ספציפית
            updateArrivalTimeForKU101(conn, "14:30");

            // 2. עדכון טיסות מפריז לפני 15:00
            updateArrivalTimeForParisBefore15(conn, "14:50");

            // 3. עדכון ידני מהמשתמש
            updateFlightsManually(conn);

            // 4. הצגת הנתונים לאחר השינויים
            printAllFlights(conn);

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    // 1. עדכון זמן הגעה לטיסה KU101
    public static void updateArrivalTimeForKU101(Connection conn, String newArrivalTime) throws SQLException {
        String sql = "UPDATE Flights SET delay = ? WHERE flight = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newArrivalTime);
            ps.setString(2, "KU101");
            int updated = ps.executeUpdate();
            System.out.println("Rows updated for KU101: " + updated);
        }
    }

    // 2. עדכון לכל טיסה מפריז שמגיעה לפני 15:00
    public static void updateArrivalTimeForParisBefore15(Connection conn, String newArrivalTime) throws SQLException {
        String sql = "UPDATE Flights SET delay = ? WHERE `from` = ? AND scheduled < ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newArrivalTime);
            ps.setString(2, "Paris");
            ps.setString(3, "15:00");
            int updated = ps.executeUpdate();
            System.out.println("Rows updated for Paris flights before 15:00: " + updated);
        }
    }

    // 3. עדכון ידני של זמן ההגעה לפי קלט מהמשתמש
    public static void updateFlightsManually(Connection conn) throws SQLException {
        List<String> flights = new ArrayList<>();

        // שליפת כל הטיסות והדפסתן
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT flight, delay FROM Flights")) {
            while (rs.next()) {
                String flight = rs.getString("flight");
                String delay = rs.getString("delay");
                System.out.println("Flight: " + flight + ", Current Delay: " + delay);
                flights.add(flight);
            }
        }

        // קלט מהמשתמש לעדכון
        try (Scanner scanner = new Scanner(System.in)) {
            for (String flight : flights) {
                System.out.print("Enter new arrival time for flight " + flight + " (leave blank to skip): ");
                String newDelay = scanner.nextLine();
                if (!newDelay.trim().isEmpty()) {
                    String sql = "UPDATE Flights SET delay = ? WHERE flight = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, newDelay);
                        ps.setString(2, flight);
                        int updated = ps.executeUpdate();
                        System.out.println("Updated flight " + flight + ": " + updated + " row(s)");
                    }
                }
            }
        }
    }

    // 4. הדפסת כל הטיסות בטבלה
    public static void printAllFlights(Connection conn) throws SQLException {
        String sql = "SELECT scheduled, flight, `from`, delay, terminal FROM Flights";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nFinal list of Flights:");
            while (rs.next()) {
                System.out.println("Scheduled: " + rs.getString("scheduled") +
                        ", Flight: " + rs.getString("flight") +
                        ", From: " + rs.getString("from") +
                        ", Delay: " + rs.getString("delay") +
                        ", Terminal: " + rs.getString("terminal"));
            }
        }
    }
}


