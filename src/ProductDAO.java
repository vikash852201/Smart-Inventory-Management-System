import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Insert new product or update quantity if product exists.
    public static void addOrIncrementProduct(String name, int qtyToAdd, double price) throws SQLException {
        String selectSql = "SELECT id, quantity FROM products WHERE name = ?";
        String insertSql = "INSERT INTO products(name, quantity, price) VALUES(?,?,?)";
        String updateSql = "UPDATE products SET quantity = quantity + ?, price = ? WHERE id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psSelect = con.prepareStatement(selectSql)) {
                psSelect.setString(1, name);
                ResultSet rs = psSelect.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt("id");
                    try (PreparedStatement psUpdate = con.prepareStatement(updateSql)) {
                        psUpdate.setInt(1, qtyToAdd);
                        psUpdate.setDouble(2, price);
                        psUpdate.setInt(3, id);
                        psUpdate.executeUpdate();
                    }
                } else {
                    try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
                        psInsert.setString(1, name);
                        psInsert.setInt(2, qtyToAdd);
                        psInsert.setDouble(3, price);
                        psInsert.executeUpdate();
                    }
                }
            }
            con.commit();
        } catch (SQLException ex) {
            throw ex;
        }
    }

    // Sell item safely (transactional) - returns new quantity, or -1 if not enough stock, or -2 if not found
    public static int sellProduct(String name, int sellQty) throws SQLException {
        String selectForUpdate = "SELECT id, quantity FROM products WHERE name = ? FOR UPDATE";
        String updateSql = "UPDATE products SET quantity = ? WHERE id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(selectForUpdate)) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    con.rollback();
                    return -2; // not found
                }
                int id = rs.getInt("id");
                int currentQty = rs.getInt("quantity");
                if (currentQty < sellQty) {
                    con.rollback();
                    return -1; // not enough
                }
                int newQty = currentQty - sellQty;
                try (PreparedStatement psUpd = con.prepareStatement(updateSql)) {
                    psUpd.setInt(1, newQty);
                    psUpd.setInt(2, id);
                    psUpd.executeUpdate();
                }
                con.commit();
                return newQty;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public static List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, quantity, price FROM products ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        }
        return list;
    }
}
