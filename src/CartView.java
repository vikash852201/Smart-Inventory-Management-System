import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class CartView extends JFrame {
    private JTable cartTable;
    private JButton removeButton, checkoutButton, refreshButton;
    private String username;
    private JLabel totalLabel;

    public CartView(String username) {
        this.username = username;

        setTitle("Your Cart - Smart Inventory Manager");
        setSize(800, 450);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---------- HEADER ----------
        JLabel headerLabel = new JLabel("Your Cart", SwingConstants.CENTER);
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
        headerPanel.setPreferredSize(new Dimension(800, 60));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ---------- TABLE ----------
        cartTable = new JTable();
        cartTable.setRowHeight(28);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        cartTable.getTableHeader().setBackground(new Color(240, 240, 240));
        cartTable.setGridColor(new Color(230, 230, 230));
        cartTable.setShowHorizontalLines(true);
        cartTable.setSelectionBackground(new Color(173, 216, 230));
        cartTable.setSelectionForeground(Color.BLACK);

        // Alternate row colors
        cartTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        JScrollPane scrollPane = new JScrollPane(cartTable);
        add(scrollPane, BorderLayout.CENTER);

        // ---------- TOTAL LABEL ----------
        totalLabel = new JLabel("Grand Total: ₹0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 20));
        totalLabel.setForeground(new Color(52, 143, 80));
        add(totalLabel, BorderLayout.NORTH);

        // ---------- BUTTON PANEL ----------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 12));
        btnPanel.setBackground(Color.WHITE);

        removeButton = createStyledButton("Remove Selected");
        checkoutButton = createStyledButton("Checkout");
        refreshButton = createStyledButton("Refresh");

        btnPanel.add(removeButton);
        btnPanel.add(checkoutButton);
        btnPanel.add(refreshButton);

        add(btnPanel, BorderLayout.SOUTH);

        // ---------- EVENT HANDLERS ----------
        refreshButton.addActionListener(e -> loadCartItems());
        removeButton.addActionListener(e -> removeSelectedItem());
        checkoutButton.addActionListener(e -> checkoutItems());

        loadCartItems();

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
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(60, 160, 90));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(52, 143, 80));
            }
        });
        return btn;
    }

    private void loadCartItems() {
        try (Connection con = DBConnection.getConnection()) {
            String query = """
                SELECT c.id, p.name, p.category, p.price, c.quantity, (p.price * c.quantity) AS total
                FROM cart c
                JOIN products p ON c.product_id = p.id
                WHERE c.username = ?
            """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            String[] cols = {"Cart ID", "Product Name", "Category", "Price", "Quantity", "Total"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            double grandTotal = 0;

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getDouble("total")
                };
                grandTotal += rs.getDouble("total");
                model.addRow(row);
            }

            cartTable.setModel(model);
            totalLabel.setText("Grand Total: ₹" + String.format("%.2f", grandTotal));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }

        int cartId = (int) cartTable.getValueAt(selectedRow, 0);
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM cart WHERE id = ?");
            ps.setInt(1, cartId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Item removed!");
            loadCartItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkoutItems() {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // Step 1: Get cart items
            PreparedStatement psCart = con.prepareStatement(
                "SELECT c.product_id, c.quantity, p.price, p.quantity AS stock " +
                "FROM cart c JOIN products p ON c.product_id = p.id WHERE c.username = ?"
            );
            psCart.setString(1, username);
            ResultSet rs = psCart.executeQuery();

            double totalAmount = 0;
            boolean stockOkay = true;
            java.util.List<int[]> items = new java.util.ArrayList<>();

            while (rs.next()) {
                int productId = rs.getInt("product_id");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");

                if (qty > stock) {
                    JOptionPane.showMessageDialog(this,
                        "⚠️ Product ID " + productId + " is out of stock. Reduce quantity!");
                    stockOkay = false;
                    break;
                }

                items.add(new int[]{productId, qty});
                totalAmount += price * qty;
            }

            if (!stockOkay) {
                con.rollback();
                return;
            }

            // Step 2: Create order
            PreparedStatement psOrder = con.prepareStatement(
                "INSERT INTO orders (username, total_amount, status) VALUES (?, ?, 'Confirmed')",
                Statement.RETURN_GENERATED_KEYS
            );
            psOrder.setString(1, username);
            psOrder.setDouble(2, totalAmount);
            psOrder.executeUpdate();

            ResultSet rsOrder = psOrder.getGeneratedKeys();
            rsOrder.next();
            int orderId = rsOrder.getInt(1);

            // Step 3: Add items & update stock
            for (int[] item : items) {
                int productId = item[0];
                int qty = item[1];

                PreparedStatement psItem = con.prepareStatement(
                    "INSERT INTO order_items (order_id, product_id, quantity, price) " +
                    "SELECT ?, id, ?, price FROM products WHERE id = ?"
                );
                psItem.setInt(1, orderId);
                psItem.setInt(2, qty);
                psItem.setInt(3, productId);
                psItem.executeUpdate();

                PreparedStatement psStock = con.prepareStatement(
                    "UPDATE products SET quantity = quantity - ? WHERE id = ?"
                );
                psStock.setInt(1, qty);
                psStock.setInt(2, productId);
                psStock.executeUpdate();
            }

            // Step 4: Clear cart
            PreparedStatement psClear = con.prepareStatement("DELETE FROM cart WHERE username = ?");
            psClear.setString(1, username);
            psClear.executeUpdate();

            con.commit();

            JOptionPane.showMessageDialog(this,
                "✅ Checkout successful!\nOrder ID: " + orderId + "\nTotal: ₹" + totalAmount);

            loadCartItems();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CartView("prem");
    }
}
