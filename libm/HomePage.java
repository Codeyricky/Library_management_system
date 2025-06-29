import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HomePage {
    private JFrame frame;
    private JTextField bookIdField;
    private JTextArea bookListArea;
    private String role;

    public HomePage(String role) {
        this.role = role;

        frame = new JFrame("📚 Library Management System");
        frame.setSize(750, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(78, 52, 46)); // Deep Brown
        JLabel headerLabel = new JLabel("📚 Welcome to Library Management System", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        contentPanel.setBackground(new Color(245, 225, 218)); // Light Beige

        // Left Panel - View Books
        JPanel leftPanel = createGradientPanel();
        leftPanel.setLayout(new BorderLayout());

        bookListArea = new JTextArea(12, 30);
        bookListArea.setEditable(false);
        bookListArea.setFont(new Font("Serif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(bookListArea);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel viewPanel = new JPanel();
        JButton viewBooksButton = createStyledButton("📚 View Available Books", new Color(46, 125, 50)); // Green
        viewBooksButton.addActionListener(e -> bookListArea.setText(DatabaseOperations.viewAvailableBooks()));
        viewPanel.add(viewBooksButton);
        leftPanel.add(viewPanel, BorderLayout.SOUTH);

        contentPanel.add(leftPanel);

        // Right Panel - Apply for Book
        JPanel rightPanel = createGradientPanel();
        rightPanel.setLayout(new GridLayout(3, 1, 10, 10));

        bookIdField = new JTextField(10);
        bookIdField.setFont(new Font("Serif", Font.PLAIN, 14));
        JButton applyButton = createStyledButton("📩 Apply for Book", new Color(245, 124, 0)); // Orange

        rightPanel.add(new JLabel("🔢 Enter Book ID:", SwingConstants.CENTER));
        rightPanel.add(bookIdField);
        rightPanel.add(applyButton);

        applyButton.addActionListener(e -> DatabaseOperations.applyForBook(bookIdField.getText()));

        contentPanel.add(rightPanel);

        frame.add(contentPanel, BorderLayout.CENTER);

        // Admin Panel
        if (role.equals("admin")) {
            JPanel adminPanel = createGradientPanel();
            adminPanel.setLayout(new GridLayout(5, 2, 10, 10));

            JTextField titleField = new JTextField(20);
            JTextField authorField = new JTextField(20);
            JButton addButton = createStyledButton("➕ Add Book", new Color(21, 101, 192)); // Blue
            JButton issueButton = createStyledButton("📕 Issue Book", new Color(211, 47, 47)); // Red
            JButton returnButton = createStyledButton("📗 Return Book", new Color(56, 142, 60)); // Green
            JButton viewIssuedButton = createStyledButton("📜 View Issued Books", new Color(106, 27, 154)); // Purple

            adminPanel.add(new JLabel("📖 Title:"));
            adminPanel.add(titleField);
            adminPanel.add(new JLabel("✍️ Author:"));
            adminPanel.add(authorField);
            adminPanel.add(addButton);
            adminPanel.add(issueButton);
            adminPanel.add(returnButton);
            adminPanel.add(viewIssuedButton);

            addButton.addActionListener(e -> DatabaseOperations.addBook(titleField.getText(), authorField.getText()));
            issueButton.addActionListener(e -> {
                String username = JOptionPane.showInputDialog("Enter username to issue the book:");
                if (username != null && !username.trim().isEmpty()) {
                    DatabaseOperations.issueBook(bookIdField.getText(), username);
                } else {
                    JOptionPane.showMessageDialog(null, "❌ Username cannot be empty!");
                }
            });
            returnButton.addActionListener(e -> DatabaseOperations.returnBook(bookIdField.getText()));
            viewIssuedButton.addActionListener(e -> bookListArea.setText(DatabaseOperations.viewIssuedBooks()));

            frame.add(adminPanel, BorderLayout.SOUTH);
        }

        frame.setVisible(true);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Serif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(244, 216, 192); // Light Beige
                Color color2 = new Color(204, 153, 102); // Warm Brown
                GradientPaint gradient = new GradientPaint(0, 0, color1, width, height, color2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };
    }
}