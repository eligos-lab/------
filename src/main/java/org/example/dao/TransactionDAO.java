package org.example.dao;

import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.Category;
import org.example.model.CategoryType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (type, amount, category_id, transaction_date, description) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, transaction.getType().name());
            stmt.setBigDecimal(2, transaction.getAmount());

            if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {
                stmt.setLong(3, transaction.getCategory().getId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.setTimestamp(4, Timestamp.valueOf(transaction.getDate()));
            stmt.setString(5, transaction.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении транзакции: " + e.getMessage());
        }
        return false;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.id as cat_id, c.name as cat_name, c.type as cat_type, c.color as cat_color " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "ORDER BY t.transaction_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех транзакций: " + e.getMessage());
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.id as cat_id, c.name as cat_name, c.type as cat_type, c.color as cat_color " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.transaction_date BETWEEN ? AND ? " +
                "ORDER BY t.transaction_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении транзакций по диапазону дат: " + e.getMessage());
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByCategory(Long categoryId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.id as cat_id, c.name as cat_name, c.type as cat_type, c.color as cat_color " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.category_id = ? " +
                "ORDER BY t.transaction_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении транзакций по категории: " + e.getMessage());
        }
        return transactions;
    }

    public BigDecimal getTotalIncome() {
        return getTotalByType(TransactionType.INCOME);
    }

    public BigDecimal getTotalExpense() {
        return getTotalByType(TransactionType.EXPENSE);
    }

    private BigDecimal getTotalByType(TransactionType type) {
        String sql = "SELECT SUM(amount) as total FROM transactions WHERE type = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении общей суммы: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public boolean deleteTransaction(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении транзакции: " + e.getMessage());
        }
        return false;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setType(TransactionType.valueOf(rs.getString("type")));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        transaction.setDescription(rs.getString("description"));
        if (rs.getTimestamp("created_at") != null) {
            transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        } else {
            transaction.setCreatedAt(LocalDateTime.now());
        }

        // Если есть категория
        Long categoryId = rs.getLong("category_id");
        if (!rs.wasNull()) {
            Category category = new Category();
            category.setId(rs.getLong("cat_id"));
            category.setName(rs.getString("cat_name"));

            String catType = rs.getString("cat_type");
            if (catType != null) {
                category.setType(CategoryType.valueOf(catType));
            }

            category.setColor(rs.getString("cat_color"));
            transaction.setCategory(category);
        }

        return transaction;
    }
}