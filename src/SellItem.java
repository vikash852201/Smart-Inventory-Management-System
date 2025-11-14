import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SellItem with:
 * - improved UI
 * - transactional checkout into orders + order_items
 * - auto-adds orders.invoice_number column if missing
 * - generates invoice "INV<yyyyMMddHHmmss>-<orderId>" and updates the order
 */
public class SellItem extends JFrame {
    private final JComboBox<String> productCombo = new JComboBox<>();
    private final JTextField searchField = new JTextField(18);
    private final JTextField qtyField = new JTextField(5);
    private final JTextField priceField = new JTextField(8);
    private final JTextField stockField = new JTextField(6);
    private final JTextField customerField = new JTextField(18);
    private final JTextField phoneField = new JTextField(12);

    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Product", "Qty", "Price", "Total"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(model);
    private final JLabel totalLabel = new JLabel("Total: ₹0.00");
    private final JTextArea billArea = new JTextArea();

    private Connection conn;

    public SellItem() {
        super("Sell Items — Smart Inventory");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(980, 630);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        add(topPanel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Product:"));
        productCombo.setPreferredSize(new Dimension(260, 28));
        searchPanel.add(productCombo);
        topPanel.add(searchPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Qty:"));
        qtyField.setHorizontalAlignment(JTextField.RIGHT);
        qtyField.setPreferredSize(new Dimension(60, 26));
        infoPanel.add(qtyField);
        infoPanel.add(new JLabel("Price:"));
        priceField.setPreferredSize(new Dimension(90, 26));
        priceField.setHorizontalAlignment(JTextField.RIGHT);
        infoPanel.add(priceField);
        infoPanel.add(new JLabel("Stock:"));
        stockField.setEditable(false);
        stockField.setPreferredSize(new Dimension(70, 26));
        infoPanel.add(stockField);
        JButton addBtn = new JButton("Add to Cart");
        addBtn.setPreferredSize(new Dimension(120, 30));
        infoPanel.add(addBtn);
        topPanel.add(infoPanel, BorderLayout.EAST);

        // Center table
        cartTable.setRowHeight(26);
        cartTable.getTableHeader().setReorderingAllowed(false);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane centerScroll = new JScrollPane(cartTable);
        centerScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0,12,0,12),
                centerScroll.getBorder()));
        add(centerScroll, BorderLayout.CENTER);

        // Right: customer and actions
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 12));
        add(rightPanel, BorderLayout.EAST);
        rightPanel.setPreferredSize(new Dimension(340, 0));

        JPanel custPanel = new JPanel(new GridBagLayout());
        custPanel.setBorder(BorderFactory.createTitledBorder("Customer"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0;
        custPanel.add(new JLabel("Name:"), g);
        g.gridx = 1;
        custPanel.add(customerField, g);
        g.gridx = 0; g.gridy = 1;
        custPanel.add(new JLabel("Phone:"), g);
        g.gridx = 1;
        custPanel.add(phoneField, g);

        rightPanel.add(custPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(8,8,8,8);
        a.gridx = 0; a.gridy = 0; a.gridwidth = 2;
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
        actionPanel.add(totalLabel, a);

        a.gridwidth = 1; a.gridy = 1; a.gridx = 0;
        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.setPreferredSize(new Dimension(140, 34));
        actionPanel.add(checkoutBtn, a);

        a.gridx = 1;
        JButton showBtn = new JButton("Show Bill");
        showBtn.setPreferredSize(new Dimension(140, 34));
        actionPanel.add(showBtn, a);

        a.gridy = 2; a.gridx = 0;
        JButton printBtn = new JButton("Print Bill");
        printBtn.setPreferredSize(new Dimension(140, 34));
        actionPanel.add(printBtn, a);

        a.gridx = 1;
        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.setPreferredSize(new Dimension(140, 34));
        actionPanel.add(clearBtn, a);

        rightPanel.add(actionPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        bottom.setBorder(BorderFactory.createEmptyBorder(0,12,8,12));
        JLabel help = new JLabel("<html><small>Tip: double-click a row to edit qty, right-click to remove.</small></html>");
        bottom.add(help);
        add(bottom, BorderLayout.SOUTH);

        // Context menu
        JPopupMenu tableMenu = new JPopupMenu();
        JMenuItem editQty = new JMenuItem("Edit Quantity...");
        JMenuItem removeRow = new JMenuItem("Remove Row");
        tableMenu.add(editQty);
        tableMenu.add(removeRow);

        cartTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && cartTable.getSelectedRow() != -1) {
                    editQuantityAtRow(cartTable.getSelectedRow());
                } else if (SwingUtilities.isRightMouseButton(e) && cartTable.getSelectedRow() != -1) {
                    tableMenu.show(cartTable, e.getX(), e.getY());
                }
            }
        });

        editQty.addActionListener(e -> {
            int r = cartTable.getSelectedRow();
            if (r != -1) editQuantityAtRow(r);
        });

        removeRow.addActionListener(e -> {
            int r = cartTable.getSelectedRow();
            if (r != -1) {
                model.removeRow(r);
                updateTotal();
            }
        });

        // Events
        addBtn.addActionListener(e -> addToCart());
        checkoutBtn.addActionListener(e -> checkout());
        showBtn.addActionListener(e -> showBill());
        printBtn.addActionListener(e -> printBill());
        clearBtn.addActionListener(e -> { model.setRowCount(0); updateTotal(); });

        productCombo.addActionListener(e -> onProductSelected());
        searchField.getDocument().addDocumentListener((SimpleDocumentListener) this::filterProducts);

        // DB init
        try {
            conn = DBConnection.getConnection();
            loadProducts();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB connection error: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        pack();
        setMinimumSize(new Dimension(900, 560));
        setVisible(true);
    }

    private void loadProducts() {
        if (conn == null) return;
        try (PreparedStatement st = conn.prepareStatement("SELECT name FROM products ORDER BY name")) {
            try (ResultSet rs = st.executeQuery()) {
                productCombo.removeAllItems();
                while (rs.next()) productCombo.addItem(rs.getString(1));
            }
            onProductSelected();
        } catch (SQLException ex) {
            showError("Error loading products: " + ex.getMessage(), ex);
        }
    }

    private void filterProducts() {
        String term = searchField.getText().trim();
        if (conn == null) return;
        if (term.isEmpty()) {
            loadProducts();
            return;
        }
        try (PreparedStatement st = conn.prepareStatement("SELECT name FROM products WHERE name LIKE ? ORDER BY name")) {
            st.setString(1, "%" + term + "%");
            try (ResultSet rs = st.executeQuery()) {
                productCombo.removeAllItems();
                while (rs.next()) productCombo.addItem(rs.getString(1));
            }
            onProductSelected();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void onProductSelected() {
        String name = (String) productCombo.getSelectedItem();
        if (name == null) {
            priceField.setText("");
            stockField.setText("");
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT price, quantity FROM products WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    priceField.setText(String.format("%.2f", rs.getDouble("price")));
                    stockField.setText(String.valueOf(rs.getInt("quantity")));
                } else {
                    priceField.setText("");
                    stockField.setText("");
                }
            }
        } catch (SQLException ex) {
            showError("Error fetching product info: " + ex.getMessage(), ex);
        }
    }

    private void addToCart() {
        String product = (String) productCombo.getSelectedItem();
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }
        String qtyS = qtyField.getText().trim();
        String priceS = priceField.getText().trim();
        if (qtyS.isEmpty() || priceS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter quantity and ensure product price is loaded.");
            return;
        }
        int qty;
        double price;
        try {
            qty = Integer.parseInt(qtyS);
            price = Double.parseDouble(priceS);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity or price.");
            return;
        }
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be > 0.");
            return;
        }

        // check stock
        try (PreparedStatement ps = conn.prepareStatement("SELECT quantity FROM products WHERE name = ?")) {
            ps.setString(1, product);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int stock = rs.getInt(1);
                    if (qty > stock) {
                        JOptionPane.showMessageDialog(this, "Not enough stock. Available: " + stock);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Product not in DB.");
                    return;
                }
            }
        } catch (SQLException ex) {
            showError("DB error: " + ex.getMessage(), ex);
            return;
        }

        double total = qty * price;
        model.addRow(new Object[]{product, qty, price, total});
        updateTotal();
        qtyField.setText("");
        qtyField.requestFocus();
    }

    private void editQuantityAtRow(int row) {
        String prod = (String) model.getValueAt(row, 0);
        int old = Integer.parseInt(String.valueOf(model.getValueAt(row, 1)));
        String s = JOptionPane.showInputDialog(this, "Enter new quantity for " + prod + ":", old);
        if (s == null) return;
        int nq;
        try {
            nq = Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number.");
            return;
        }
        if (nq <= 0) { JOptionPane.showMessageDialog(this, "Quantity must be > 0."); return; }

        // check stock before changing
        try (PreparedStatement ps = conn.prepareStatement("SELECT quantity FROM products WHERE name = ?")) {
            ps.setString(1, prod);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int stock = rs.getInt(1);
                    if (nq > stock) {
                        JOptionPane.showMessageDialog(this, "Not enough stock. Available: " + stock);
                        return;
                    }
                }
            }
        } catch (SQLException ex) {
            showError("DB error: " + ex.getMessage(), ex);
            return;
        }

        double price = Double.parseDouble(String.valueOf(model.getValueAt(row, 2)));
        model.setValueAt(nq, row, 1);
        model.setValueAt(nq * price, row, 3);
        updateTotal();
    }

    private void updateTotal() {
        double sum = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            sum += Double.parseDouble(String.valueOf(model.getValueAt(i, 3)));
        }
        totalLabel.setText("Total: ₹" + String.format("%.2f", sum));
    }

    /**
     * Main checkout logic:
     * - ensures orders.invoice_number column exists (adds if missing)
     * - inserts an orders row (username=NULL for POS)
     * - for each cart line: locks product row (FOR UPDATE), validates stock, updates stock, inserts order_items
     * - generates invoice number "INV<yyyyMMddHHmmss>-<orderId>" and updates orders.invoice_number
     * - commits or rolls back as needed
     */
    private void checkout() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No database connection.");
            return;
        }

        String customer = customerField.getText().trim();
        String phone = phoneField.getText().trim();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // compute total amount
        double totalAmount = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            totalAmount += Double.parseDouble(String.valueOf(model.getValueAt(i, 3)));
        }

        try {
            // ensure invoice_number column exists (safe ALTER if not present)
            ensureInvoiceColumn();

            conn.setAutoCommit(false);

            // 1) insert order (username NULL for POS)
            String orderSql = "INSERT INTO orders (username, order_date, total_amount, status, shipping_address) VALUES (?, ?, ?, 'Confirmed', ?)";
            try (PreparedStatement psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setNull(1, Types.VARCHAR);
                psOrder.setString(2, dateTime);
                psOrder.setDouble(3, totalAmount);
                String shipping = (customer.isEmpty() ? "Walk-in" : customer) + (phone.isEmpty() ? "" : " | " + phone);
                psOrder.setString(4, shipping);
                psOrder.executeUpdate();

                ResultSet rsKey = psOrder.getGeneratedKeys();
                if (!rsKey.next()) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Checkout failed: could not create order record.");
                    conn.setAutoCommit(true);
                    return;
                }
                int orderId = rsKey.getInt(1);

                // 2) for each cart line, lock product row, check stock, update, insert order_items
                String selectForUpdate = "SELECT id, quantity, price FROM products WHERE name = ? FOR UPDATE";
                String updateStockSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
                String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

                try (PreparedStatement psSelect = conn.prepareStatement(selectForUpdate);
                     PreparedStatement psUpdateStock = conn.prepareStatement(updateStockSql);
                     PreparedStatement psInsertItem = conn.prepareStatement(insertItemSql)) {

                    for (int i = 0; i < model.getRowCount(); i++) {
                        String productName = model.getValueAt(i, 0).toString();
                        int qty = Integer.parseInt(model.getValueAt(i, 1).toString());
                        double price = Double.parseDouble(model.getValueAt(i, 2).toString());

                        psSelect.setString(1, productName);
                        try (ResultSet rs = psSelect.executeQuery()) {
                            if (!rs.next()) {
                                conn.rollback();
                                JOptionPane.showMessageDialog(this, "Checkout failed: product not found - " + productName);
                                conn.setAutoCommit(true);
                                return;
                            }
                            int productId = rs.getInt("id");
                            int stock = rs.getInt("quantity");

                            if (qty > stock) {
                                conn.rollback();
                                JOptionPane.showMessageDialog(this, "Checkout failed: insufficient stock for " + productName + " (available: " + stock + ")");
                                conn.setAutoCommit(true);
                                return;
                            }

                            psUpdateStock.setInt(1, qty);
                            psUpdateStock.setInt(2, productId);
                            psUpdateStock.executeUpdate();

                            psInsertItem.setInt(1, orderId);
                            psInsertItem.setInt(2, productId);
                            psInsertItem.setInt(3, qty);
                            psInsertItem.setDouble(4, price);
                            psInsertItem.executeUpdate();
                        }
                    }
                }

                // 3) generate invoice number and update order
                String invoice = generateInvoiceString(orderId);
                String updateInvoiceSql = "UPDATE orders SET invoice_number = ? WHERE id = ?";
                try (PreparedStatement psInvoice = conn.prepareStatement(updateInvoiceSql)) {
                    psInvoice.setString(1, invoice);
                    psInvoice.setInt(2, orderId);
                    psInvoice.executeUpdate();
                }

                conn.commit();
                conn.setAutoCommit(true);

                JOptionPane.showMessageDialog(this,
                        "✅ Checkout successful!\nOrder ID: " + orderId + "\nInvoice: " + invoice + "\nTotal: ₹" + String.format("%.2f", totalAmount));

                model.setRowCount(0);
                updateTotal();
                loadProducts(); // refresh stock
            }
        } catch (SQLException ex) {
            try { if (conn != null) conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            showError("Checkout error: " + ex.getMessage(), ex);
        }
    }

    // Try to add invoice_number column if it does not exist
    private void ensureInvoiceColumn() {
        try {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, "orders", "invoice_number")) {
                if (rs.next()) {
                    return; // column exists
                }
            }
            // add column safely
            String alter = "ALTER TABLE orders ADD COLUMN invoice_number VARCHAR(64) NULL UNIQUE";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(alter);
            }
        } catch (SQLException ex) {
            // non-fatal: if alter fails (permissions, etc.), proceed without invoice column
            System.err.println("Warning: could not ensure invoice_number column: " + ex.getMessage());
        }
    }

    private String generateInvoiceString(int orderId) {
        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return "INV" + ts + "-" + orderId;
    }

    private void showBill() {
        StringBuilder bill = new StringBuilder();
        bill.append("********** BILL RECEIPT **********\n");
        bill.append("Customer: ").append(customerField.getText().isEmpty() ? "Walk-in" : customerField.getText()).append("\n");
        bill.append("Phone: ").append(phoneField.getText().isEmpty() ? "-" : phoneField.getText()).append("\n");
        bill.append("Date: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
        bill.append("----------------------------------\n");
        bill.append(String.format("%-20s %-5s %-10s %-10s\n", "Product", "Qty", "Price", "Total"));
        bill.append("----------------------------------\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            bill.append(String.format("%-20s %-5s %-10s %-10s\n",
                    model.getValueAt(i, 0),
                    model.getValueAt(i, 1),
                    model.getValueAt(i, 2),
                    model.getValueAt(i, 3)));
        }
        bill.append("----------------------------------\n");
        bill.append(totalLabel.getText()).append("\n");
        bill.append("********** THANK YOU **********\n");

        billArea.setText(bill.toString());
        JOptionPane.showMessageDialog(this, new JScrollPane(billArea), "Bill Preview", JOptionPane.PLAIN_MESSAGE);
    }

    private void printBill() {
        try {
            if (billArea.getText().isEmpty()) showBill();
            billArea.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing Error: " + e.getMessage());
        }
    }

    private void showError(String msg, Exception ex) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    @FunctionalInterface
    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();
        @Override default void insertUpdate(DocumentEvent e) { update(); }
        @Override default void removeUpdate(DocumentEvent e) { update(); }
        @Override default void changedUpdate(DocumentEvent e) { update(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SellItem::new);
    }
}
