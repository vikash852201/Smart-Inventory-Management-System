import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerDashboard extends JFrame {
    private JTable productTable;
    private JButton refreshButton, cartBtn, ordersBtn;
    private String username;

    public CustomerDashboard(String username) {
        this.username = username;

        setTitle("Customer Panel - Smart Inventory Manager");
        setSize(800, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ---------- HEADER ----------
        JLabel headerLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(52, 143, 80);
                Color color2 = new Color(86, 180, 211);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.setPreferredSize(new Dimension(800, 60));

        add(headerPanel, BorderLayout.NORTH);

        // ---------- TABLE ----------
        productTable = new JTable();
        productTable.setRowHeight(28);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        productTable.getTableHeader().setBackground(new Color(240, 240, 240));
        productTable.setGridColor(new Color(230, 230, 230));
        productTable.setShowHorizontalLines(true);
        productTable.setSelectionBackground(new Color(173, 216, 230));
        productTable.setSelectionForeground(Color.BLACK);

        // Alternate row color
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return c;
            }
        });

        add(new JScrollPane(productTable), BorderLayout.CENTER);

        // ---------- BUTTON PANEL ----------
        refreshButton = createStyledButton("Refresh Products");
        cartBtn = createStyledButton("View Cart");
        ordersBtn = createStyledButton("My Orders");

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 12));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(refreshButton);
        bottomPanel.add(cartBtn);
        bottomPanel.add(ordersBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // ---------- BUTTON ACTIONS ----------
        refreshButton.addActionListener(e -> loadProducts());
        cartBtn.addActionListener(e -> new CartView(username));
        ordersBtn.addActionListener(e -> new MyOrders(username));

        loadProducts();

        setLocationRelativeTo(null);
        setVisible(true);
    }

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

    private void loadProducts() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, name, price, quantity FROM products"
             );
             ResultSet rs = ps.executeQuery()) {

            String[] cols = {"ID", "Name", "Price", "Stock", "Action"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return col == 4;
                }
            };

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("quantity");

                String status = (stock > 0) ? "Add to Cart" : "Out of Stock";
                model.addRow(new Object[]{id, name, price, stock, status});
            }

            productTable.setModel(model);
            addButtonToTable();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addButtonToTable() {
        productTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        productTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), username));
    }

    // ----- BUTTON RENDERER -----
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            boolean inStock = !value.toString().equals("Out of Stock");
            setBackground(inStock ? new Color(52, 143, 80) : Color.LIGHT_GRAY);
            setForeground(Color.WHITE);
            setEnabled(inStock);
            return this;
        }
    }

    // ----- BUTTON EDITOR -----
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int productId;
        private String username;

        public ButtonEditor(JCheckBox checkBox, String username) {
            super(checkBox);
            this.username = username;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(52, 143, 80));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            productId = (int) table.getValueAt(row, 0);
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked && !label.equals("Out of Stock")) {
                addToCart(productId);
            }
            clicked = false;
            return label;
        }

        private void addToCart(int productId) {
            String qtyStr = JOptionPane.showInputDialog(null, "Enter quantity:", "Add to Cart", JOptionPane.PLAIN_MESSAGE);
            if (qtyStr == null || qtyStr.isEmpty()) return;

            int quantity;
            try {
                quantity = Integer.parseInt(qtyStr);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid quantity!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid number format!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement psCheck = con.prepareStatement("SELECT quantity FROM products WHERE id = ?");
                psCheck.setInt(1, productId);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    int stock = rs.getInt("quantity");
                    if (quantity > stock) {
                        JOptionPane.showMessageDialog(null, "Not enough stock available!");
                        return;
                    }

                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO cart (username, product_id, quantity) VALUES (?, ?, ?)"
                    );
                    ps.setString(1, username);
                    ps.setInt(2, productId);
                    ps.setInt(3, quantity);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Item added to cart!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new CustomerDashboard("prem");
    }
}
