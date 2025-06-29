import java.sql.*;
import javax.swing.*;

public class DatabaseOperations {
    private static final String URL = "jdbc:mysql://localhost:3307/libm";
    private static final String USER = "root";
    private static final String PASSWORD = "mukilan1211";

    public static void addBook(String title, String author) {
        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ Title and Author are required!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO available_books (title, author, is_available) VALUES (?, ?, TRUE)")) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "✅ Book added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "❌ Error adding book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String viewAvailableBooks() {
        StringBuilder books = new StringBuilder("📚 Available Books:\n");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM available_books WHERE is_available = TRUE")) {
            while (rs.next()) {
                books.append("ID: ").append(rs.getInt("id"))
                     .append(" | Title: ").append(rs.getString("title"))
                     .append(" | Author: ").append(rs.getString("author"))
                     .append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Error fetching books: " + e.getMessage();
        }
        return books.length() > 17 ? books.toString() : "📚 No books available.";
    }

    public static void applyForBook(String bookId) {
        if (bookId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ Please enter a Book ID!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM available_books WHERE id = ? AND is_available = TRUE")) {
            stmt.setInt(1, Integer.parseInt(bookId));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "✅ Request Sent! Admin will review.");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Book not found or already issued!");
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void issueBook(String bookId, String username) {
        if (bookId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ Please enter a Book ID!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            // Get user_id from username
            PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE name = ?");
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();
            if (!userRs.next()) {
                JOptionPane.showMessageDialog(null, "❌ User not found!");
                conn.rollback();
                return;
            }
            int userId = userRs.getInt("id");

            // Check if book exists and is available
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM available_books WHERE id = ? AND is_available = TRUE");
            checkStmt.setInt(1, Integer.parseInt(bookId));
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Insert into issued_books
                PreparedStatement issueStmt = conn.prepareStatement(
                        "INSERT INTO issued_books (book_id, user_id) VALUES (?, ?)");
                issueStmt.setInt(1, Integer.parseInt(bookId));
                issueStmt.setInt(2, userId);
                issueStmt.executeUpdate();

                // Mark book as unavailable
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE available_books SET is_available = FALSE WHERE id = ?");
                updateStmt.setInt(1, Integer.parseInt(bookId));
                updateStmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(null, "✅ Book Issued to " + username + "!");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Book not found or already issued!");
                conn.rollback();
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "❌ Error issuing book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void returnBook(String bookId) {
        if (bookId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ Please enter a Book ID!");
            return;
        }
        String username = JOptionPane.showInputDialog("Enter the username of the person returning the book:");
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Username cannot be empty!");
            return;
        }
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            // Get user_id from username
            PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE name = ?");
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();
            if (!userRs.next()) {
                JOptionPane.showMessageDialog(null, "❌ User not found!");
                conn.rollback();
                return;
            }
            int userId = userRs.getInt("id");

            // Check if book is issued to this user
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT book_id FROM issued_books WHERE book_id = ? AND user_id = ?");
            checkStmt.setInt(1, Integer.parseInt(bookId));
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Mark book as available
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE available_books SET is_available = TRUE WHERE id = ?");
                updateStmt.setInt(1, Integer.parseInt(bookId));
                updateStmt.executeUpdate();

                // Delete from issued_books
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM issued_books WHERE book_id = ? AND user_id = ?");
                deleteStmt.setInt(1, Integer.parseInt(bookId));
                deleteStmt.setInt(2, userId);
                deleteStmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(null, "✅ Book returned by " + username + "!");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Book not found or not issued to this user!");
                conn.rollback();
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "❌ Error returning book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String viewIssuedBooks() {
        StringBuilder books = new StringBuilder("📕 Issued Books:\n");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT ib.book_id, ab.title, ab.author, u.name " +
                     "FROM issued_books ib " +
                     "JOIN available_books ab ON ib.book_id = ab.id " +
                     "JOIN users u ON ib.user_id = u.id")) {
            while (rs.next()) {
                books.append("ID: ").append(rs.getInt("book_id"))
                     .append(" | Title: ").append(rs.getString("title"))
                     .append(" | Author: ").append(rs.getString("author"))
                     .append(" | Issued To: ").append(rs.getString("name"))
                     .append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "❌ Error fetching issued books: " + e.getMessage();
        }
        return books.length() > 15 ? books.toString() : "📕 No books issued.";
    }
}