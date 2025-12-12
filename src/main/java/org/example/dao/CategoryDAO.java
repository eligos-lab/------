package org.example.dao;

import org.example.model.Category;
import org.example.model.CategoryType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY type, name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                category.setType(CategoryType.valueOf(rs.getString("type")));
                category.setColor(rs.getString("color"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении категорий: " + e.getMessage());
        }
        return categories;
    }

    public List<Category> getCategoriesByType(CategoryType type) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type = ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getLong("id"));
                    category.setName(rs.getString("name"));
                    category.setType(CategoryType.valueOf(rs.getString("type")));
                    category.setColor(rs.getString("color"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении категорий по типу: " + e.getMessage());
        }
        return categories;
    }

    public Category getCategoryById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getLong("id"));
                    category.setName(rs.getString("name"));
                    category.setType(CategoryType.valueOf(rs.getString("type")));
                    category.setColor(rs.getString("color"));
                    return category;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении категории по ID: " + e.getMessage());
        }
        return null;
    }

    public boolean addCategory(Category category) {
        String sql = "INSERT INTO categories (name, type, color) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getType().name());
            stmt.setString(3, category.getColor());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении категории: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteCategory(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении категории: " + e.getMessage());
        }
        return false;
    }
}