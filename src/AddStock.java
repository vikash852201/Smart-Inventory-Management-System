import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AddStock extends JFrame {
    private JTextField nameField, qtyField, priceField;
    private JButton addButton, backButton;

    public AddStock() {
        setTitle("Add / Receive Stock");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JLabel headerLabel = new JLabel("Add / Receive Stock", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
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
        headerPanel.setPreferredSize(new Dimension(450, 60));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ---------- FORM PANEL ----------
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        nameField = new JTextField(18);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        formPanel.add(qtyLabel, gbc);

        gbc.gridx = 1;
        qtyField = new JTextField(18);
        qtyField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        qtyField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(qtyField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel priceLabel = new JLabel("Price per Unit:");
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        formPanel.add(priceLabel, gbc);

        gbc.gridx = 1;
        priceField = new JTextField(18);
        priceField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(priceField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ---------- BUTTON PANEL ----------
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 12));
        buttonPanel.setBackground(Color.WHITE);

        addButton = createStyledButton("Add Stock");
        backButton = createStyledButton("Back");

        buttonPanel.add(addButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ---------- ACTIONS ----------
        addButton.addActionListener(e -> onAdd());
        backButton.addActionListener(e -> dispose());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- STYLING ----------
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(52, 143, 80));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
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

    // ---------- LOGIC (UNCHANGED) ----------
    private void onAdd() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter product name.");
            return;
        }
        int qty;
        double price;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            price = Double.parseDouble(priceField.getText().trim());
            if (qty <= 0 || price < 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be >0 and price >=0.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers.");
            return;
        }

        try {
            ProductDAO.addOrIncrementProduct(name, qty, price);
            JOptionPane.showMessageDialog(this, "Stock successfully added/updated.");
            if (qty < 5) {
                JOptionPane.showMessageDialog(this, "⚠️ Low stock added: only " + qty + " newly added for " + name);
            }
            ViewStock.refreshTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding stock: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new AddStock();
    }
}
