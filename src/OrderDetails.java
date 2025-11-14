import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrderDetails extends JFrame {
    private JTable detailsTable;
    private JLabel titleLabel;

    public OrderDetails(int orderId) {
        setTitle("Order Details - Order #" + orderId);
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 🌈 Main panel with gradient background
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
        titleLabel = new JLabel("Order Details - #" + orderId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 60, 110));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 📋 Table styling
        detailsTable = new JTable();
        detailsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailsTable.setRowHeight(28);
        detailsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailsTable.getTableHeader().setBackground(new Color(100, 149, 237));
        detailsTable.getTableHeader().setForeground(Color.WHITE);
        detailsTable.setGridColor(new Color(220, 220, 220));
        detailsTable.setSelectionBackground(new Color(173, 216, 230));
        detailsTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 240), 2));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 🔙 Bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBackground(new Color(70, 130, 180));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        loadOrderItems(orderId);

        setVisible(true);
    }

    private void loadOrderItems(int orderId) {
        try (Connection con = DBConnection.getConnection()) {
            String query = """
                SELECT p.name, p.price, oi.quantity, (oi.price * oi.quantity) AS total
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
            """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            String[] cols = {"Product Name", "Price (₹)", "Quantity", "Total (₹)"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            while (rs.next()) {
                Object[] row = {
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getDouble("total")
                };
                model.addRow(row);
            }

            detailsTable.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new OrderDetails(1);
    }
}
