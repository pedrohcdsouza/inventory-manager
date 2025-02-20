package com.pedrohcdsouza.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.pedrohcdsouza.server.models.Product;
import com.pedrohcdsouza.server.utils.ServerLogger;

public class Database {
    // Constantes SQL
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT UNIQUE,
            price REAL,
            quantity INTEGER
        )
    """;
    private static final String INSERT_SQL = "INSERT INTO products (name, price, quantity) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE products SET name = ?, price = ?, quantity = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM products WHERE id = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM products WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM products";

    private Connection connection;

    public Database(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            ServerLogger.error("Failed to create table: " + e.getMessage());
        }
    }

    private Product createProductFromResultSet(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getInt("quantity")
        );
    }

    private PreparedStatement prepareProductStatement(PreparedStatement pstmt, Product product) throws SQLException {
        pstmt.setString(1, product.name());
        pstmt.setDouble(2, product.price());
        pstmt.setInt(3, product.quantity());
        return pstmt;
    }

    private void handleDatabaseError(String operation, SQLException e) {
        ServerLogger.error("Database operation '" + operation + "' failed: " + e.getMessage());
    }

    public Product addProduct(Product product) {
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            prepareProductStatement(pstmt, product).executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Product(rs.getInt(1), product.name(), product.price(), product.quantity());
                }
            }
            ServerLogger.info("Product added successfully: " + product);
            return product;
        } catch (SQLException e) {
            handleDatabaseError("adicionar produto", e);
            return null;
        }
    }

    public void updateProduct(Product product) {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_SQL)) {
            prepareProductStatement(pstmt, product);
            pstmt.setInt(4, product.id());
            pstmt.executeUpdate();
            ServerLogger.info("Product updated successfully: " + product);
        } catch (SQLException e) {
            handleDatabaseError("atualizar produto", e);
        }
    }

    public void removeProduct(Integer id) {
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_SQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            ServerLogger.info("Product removed successfully: ID " + id);
        } catch (SQLException e) {
            handleDatabaseError("remover produto", e);
        }
    }

    public Product getProduct(Integer id) {
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? createProductFromResultSet(rs) : null;
            }
        } catch (SQLException e) {
            handleDatabaseError("buscar produto", e);
            return null;
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                products.add(createProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            handleDatabaseError("buscar produtos", e);
        }
        return products;
    }
}
