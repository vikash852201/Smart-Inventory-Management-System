import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RecentOrdersView extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton refreshBtn, detailsBtn, backBtn;

    public RecentOrdersView() {
        setTitle("Smart Inventory Manager - Recent Orders");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JLabel headerLabel = new JLabel("RECENT ORDERS", SwingConstants.CENTER);
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
        headerPanel.setPreferredSize(new Dimension(850, 70));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ---------- TABLE ----------
        String[] columns = {"Order ID", "Customer", "Total (₹)", "Date", "Status"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(52, 143, 80));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(220, 247, 227));
        table.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        add(scrollPane, BorderLayout.CENTER);

        // ---------- BUTTON PANEL ----------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        refreshBtn = createStyledButton("Refresh");
        detailsBtn = createStyledButton("View Details");
        backBtn = createStyledButton("Back");

        buttonPanel.add(refreshBtn);
        buttonPanel.add(detailsBtn);
        buttonPanel.add(backBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // ---------- ACTIONS ----------
        refreshBtn.addActionListener(e -> loadOrders());
        detailsBtn.addActionListener(e -> openOrderDetails());
        backBtn.addActionListener(e -> dispose());

        // Load initial data
        loadOrders();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- BUTTON STYLE ----------
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(52, 143, 80));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(150, 40));

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

    // ---------- LOAD ORDERS ----------
private void loadOrders() {
    model.setRowCount(0);
    String sql = """
        SELECT id,
               COALESCE(username, shipping_address) AS customer,
               total_amount,
               order_date,
               status
        FROM orders
        ORDER BY order_date DESC
        LIMIT 20
    """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("customer"),         // now returns username OR shipping_address
                "₹" + rs.getDouble("total_amount"),
                rs.getTimestamp("order_date"),
                rs.getString("status")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "❌ Error loading orders: " + e.getMessage());
        e.printStackTrace();
    }
}


    // ---------- OPEN ORDER DETAILS ----------
    private void openOrderDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to view details.");
            return;
        }
        int orderId = (int) model.getValueAt(selectedRow, 0);
        new OrderDetails(orderId);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RecentOrdersView::new);
    }
}
