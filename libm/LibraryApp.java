import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LibraryApp {
    private static final String URL = "jdbc:mysql://localhost:3307/libm";
    private static final String USER = "root";
    private static final String PASSWORD = "mukilan1211";

    private JFrame frame;
    private JTextField emailField, nameField;
    private JPasswordField passwordField;

    public LibraryApp() {
        showLoginScreen();
    }

    private void showLoginScreen() {
        frame = new JFrame("📚 Library Management - Login");
        frame.setSize(500, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 222, 179)); // Wheat
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("🔐 Login to Your Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(101, 67, 33)); // Brown
        frame.add(title, BorderLayout.NORTH);

        JLabel emailLabel = createStyledLabel("📧 Email:");
        emailField = createStyledTextField();

        JLabel passwordLabel = createStyledLabel("🔑 Password:");
        passwordField = createStyledPasswordField();

        JButton loginButton = createStyledButton("🔓 Login", new Color(46, 125, 50)); // Green
        JButton switchButton = createStyledButton("📝 Sign Up", new Color(21, 101, 192)); // Blue

        loginButton.addActionListener(e -> handleLogin());
        switchButton.addActionListener(e -> showSignUpScreen());

        gbc.gridx = 0; gbc.gridy = 0; panel.add(emailLabel, gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(passwordLabel, gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(loginButton, gbc);
        gbc.gridy = 3; panel.add(switchButton, gbc);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void showSignUpScreen() {
        frame.getContentPane().removeAll();
        frame.setTitle("📝 Sign Up");

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(52, 73, 94)); // Dark Blue-Grey
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("👤 Create a New Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(245, 222, 179)); // Wheat
        frame.add(title, BorderLayout.NORTH);

        JLabel nameLabel = createStyledLabel("👤 Name:");
        nameField = createStyledTextField();

        JLabel emailLabel = createStyledLabel("📧 Email:");
        emailField = createStyledTextField();

        JLabel passwordLabel = createStyledLabel("🔑 Password:");
        passwordField = createStyledPasswordField();

        JLabel roleLabel = createStyledLabel("🛠️ Role (admin/user):");
        JTextField roleField = createStyledTextField();

        JButton signUpButton = createStyledButton("✅ Sign Up", new Color(46, 125, 50)); // Green
        JButton switchButton = createStyledButton("🔙 Back to Login", new Color(21, 101, 192)); // Blue

        signUpButton.addActionListener(e -> handleSignUp(roleField.getText()));
        switchButton.addActionListener(e -> showLoginScreen());

        gbc.gridx = 0; gbc.gridy = 0; panel.add(nameLabel, gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(emailLabel, gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(passwordLabel, gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(roleLabel, gbc);
        gbc.gridx = 1; panel.add(roleField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; panel.add(signUpButton, gbc);
        gbc.gridy = 5; panel.add(switchButton, gbc);

        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void handleSignUp(String role) {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "⚠️ All fields are required!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role.toLowerCase());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(frame, "✅ Account created! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            showLoginScreen();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "❌ Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT role FROM users WHERE email = ? AND password = ?")) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                frame.dispose();
                SwingUtilities.invokeLater(() -> new HomePage(role));
            } else {
                JOptionPane.showMessageDialog(frame, "❌ Invalid Email or Password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "❌ Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryApp::new);
    }
}