import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ViewStock extends JFrame {
    private static DefaultTableModel model;
    private JTable table;

    public ViewStock() {
        setTitle("View Stock - Smart Inventory Manager");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JLabel headerLabel = new JLabel("CURRENT STOCK OVERVIEW", SwingConstants.CENTER);
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
        headerPanel.setPreferredSize(new Dimension(700, 70));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ---------- TABLE ----------
        model = new DefaultTableModel(
                new String[]{"ID", "Product Name", "Quantity", "Price", "Total Value"}, 0
        );
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(200, 230, 201));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // ---------- BUTTON PANEL ----------
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton refreshBtn = createStyledButton("Refresh");
        JButton backBtn = createStyledButton("Back");

        btnPanel.add(refreshBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // ---------- ACTIONS ----------
        refreshBtn.addActionListener(e -> loadData());
        backBtn.addActionListener(e -> dispose()); // ✅ closes and returns to Dashboard

        // ---------- INITIAL LOAD ----------
        loadData();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------- STYLED BUTTON ----------
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(52, 143, 80));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 160, 90));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 143, 80));
            }
        });
        return btn;
    }

    // ---------- LOAD DATA ----------
    public static void loadData() {
        if (model == null) return;
        model.setRowCount(0);
        try {
            List<Product> list = ProductDAO.getAllProducts();
            for (Product p : list) {
                double total = p.getQuantity() * p.getPrice();
                model.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getQuantity(),
                        p.getPrice(),
                        total
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading data: " + ex.getMessage());
        }
    }

    public static void refreshTable() {
        loadData();
    }

    public static void main(String[] args) {
        new ViewStock();
    }
}