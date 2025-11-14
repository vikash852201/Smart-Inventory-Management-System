import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Dashboard extends JFrame {
    public Dashboard() {
        setTitle("Smart Inventory Manager - Dashboard");
        setSize(550, 470);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JLabel headerLabel = new JLabel("SMART INVENTORY MANAGER", SwingConstants.CENTER);
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
        headerPanel.setPreferredSize(new Dimension(550, 70));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ---------- CENTER PANEL ----------
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton addBtn = createStyledButton("Add / Receive Stock");
        JButton sellBtn = createStyledButton("Sell Item");
        JButton viewBtn = createStyledButton("View Stock");
        JButton ordersBtn = createStyledButton("Recent Orders"); // ✅ NEW BUTTON
        JButton exitBtn = createStyledButton("Exit");

        centerPanel.add(addBtn, gbc);
        gbc.gridy = 1;
        centerPanel.add(sellBtn, gbc);
        gbc.gridy = 2;
        centerPanel.add(viewBtn, gbc);
        gbc.gridy = 3;
        centerPanel.add(ordersBtn, gbc);
        gbc.gridy = 4;
        centerPanel.add(exitBtn, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // ---------- ACTIONS ----------
        addBtn.addActionListener(e -> new AddStock());
        sellBtn.addActionListener(e -> new SellItem());
        viewBtn.addActionListener(e -> new ViewStock());
        ordersBtn.addActionListener(e -> new RecentOrdersView()); // ✅ open orders window
        exitBtn.addActionListener(e -> System.exit(0));

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- STYLING ----------
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(52, 143, 80));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(230, 45));

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm());
    }
}
