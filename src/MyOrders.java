import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MyOrders extends JFrame {
    private JTable ordersTable;
    private JButton viewDetailsBtn, refreshBtn;
    private String username;

    public MyOrders(String username) {
        this.username = username;

        setTitle("My Orders - Smart Inventory Manager");
        setSize(800, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 🌈 Main background with gradient
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(240, 248, 255),
                        0, getHeight(), new Color(220, 230, 250)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        add(mainPanel);

        // 🏷️ Header
        JLabel titleLabel = new JLabel("My Orders", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 60, 110));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 📋 Orders Table styling
        ordersTable = new JTable();
        ordersTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ordersTable.setRowHeight(28);
        ordersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        ordersTable.getTableHeader().setBackground(new Color(100, 149, 237));
        ordersTable.getTableHeader().setForeground(Color.WHITE);
        ordersTable.setGridColor(new Color(220, 220, 220));
        ordersTable.setSelectionBackground(new Color(173, 216, 230));
        ordersTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 240), 2));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 🔘 Bottom panel with buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        viewDetailsBtn = new JButton("View Details");
        refreshBtn = new JButton("Refresh");

        for (JButton btn : new JButton[]{viewDetailsBtn, refreshBtn}) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBackground(new Color(70, 130, 180));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buttonPanel.add(btn);
        }

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 🎯 Event handlers (unchanged logic)
        refreshBtn.addActionListener(e -> loadOrders());
        viewDetailsBtn.addActionListener(e -> openOrderDetails());

        loadOrders();
        setVisible(true);
    }

    private void loadOrders() {
        try (Connection con = DBConnection.getConnection()) {
            String query = """
                SELECT id, order_date, total_amount, status
                FROM orders
                WHERE username = ?
                ORDER BY order_date DESC
            """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            String[] cols = {"Order ID", "Order Date", "Total (₹)", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("order_date"),
                    rs.getDouble("total_amount"),
                    rs.getString("status")
                };
                model.addRow(row);
            }

            ordersTable.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to view details.");
            return;
        }

        int orderId = (int) ordersTable.getValueAt(selectedRow, 0);
        new OrderDetails(orderId);
    }

    public static void main(String[] args) {
        new MyOrders("prem");
    }
}
