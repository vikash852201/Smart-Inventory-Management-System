import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnCancel;

    public LoginForm() {
        setTitle("Smart Inventory Manager - Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JLabel headerLabel = new JLabel("SMART INVENTORY LOGIN", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);

        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(52, 143, 80),
                        getWidth(), getHeight(), new Color(86, 180, 211)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(420, 70));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ---------- CENTER PANEL ----------
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(lblUsername, gbc);
        gbc.gridx = 1;
        centerPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(lblPassword, gbc);
        gbc.gridx = 1;
        centerPanel.add(txtPassword, gbc);

        // ---------- BUTTONS ----------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);

        btnLogin = createStyledButton("Login");
        btnCancel = createStyledButton("Cancel");
        btnPanel.add(btnLogin);
        btnPanel.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        centerPanel.add(btnPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // ---------- ACTIONS ----------
        btnLogin.addActionListener(e -> loginUser());
        btnCancel.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(52, 143, 80));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(60, 160, 90));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(52, 143, 80));
            }
        });
        return btn;
    }

    // ---------- LOGIN LOGIC ----------
    private void loginUser() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String user = rs.getString("username");

                JOptionPane.showMessageDialog(this, "✅ Login Successful as " + role + "!");
                this.dispose();

                if (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("admin")) {
                    new Dashboard().setVisible(true);
                } else if (role.equalsIgnoreCase("customer")) {
                    new CustomerDashboard(user).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "❌ Invalid Username or Password");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginForm::new);
    }
}