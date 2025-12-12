package org.example.ui;

import org.example.model.Category;
import org.example.model.CategoryType;
import org.example.service.FinanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class CategoryPanel extends JPanel {
    private FinanceService financeService;
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton refreshButton;

    public CategoryPanel() {
        this.financeService = new FinanceService();
        initComponents();
        loadCategories();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addButton = new JButton("Добавить");
        deleteButton = new JButton("Удалить");
        refreshButton = new JButton("Обновить");

        addButton.addActionListener(this::addCategory);
        deleteButton.addActionListener(this::deleteCategory);
        refreshButton.addActionListener(e -> loadCategories());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Таблица категорий
        String[] columns = {"ID", "Название", "Тип", "Цвет"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.setRowHeight(30);
        categoryTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Название
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Тип
        categoryTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Цвет

        // Устанавливаем рендерер для колонки цвета
        categoryTable.getColumnModel().getColumn(3).setCellRenderer(new ColorCellRenderer());

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список категорий"));

        add(scrollPane, BorderLayout.CENTER);

        // Информационная панель
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));
        JTextArea infoArea = new JTextArea(4, 50);
        infoArea.setText("Доступные категории по умолчанию:\n" +
                "- Доходы: Зарплата, Фриланс, Инвестиции\n" +
                "- Расходы: Продукты, Транспорт, Развлечения, Жилье, Здоровье, Образование, Прочее\n" +
                "Для удаления категории убедитесь, что в ней нет транзакций.");
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        add(infoPanel, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        tableModel.setRowCount(0);
        List<Category> categories = financeService.getAllCategories();

        for (Category category : categories) {
            Object[] row = {
                    category.getId(),
                    category.getName(),
                    category.getType() == CategoryType.INCOME ? "Доход" : "Расход",
                    category.getColor() // Просто сохраняем строку цвета
            };
            tableModel.addRow(row);
        }
    }

    private void addCategory(ActionEvent e) {
        // Диалог для добавления категории
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        JTextField nameField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Доход", "Расход"});
        JComboBox<String> colorCombo = new JComboBox<>(new String[]{
                "#FF9800", "#2196F3", "#E91E63", "#9C27B0", "#00BCD4",
                "#795548", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B"
        });
        JPanel colorPreview = new JPanel();
        colorPreview.setBackground(Color.decode("#FF9800"));
        colorPreview.setPreferredSize(new Dimension(30, 30));

        colorCombo.addActionListener(ev -> {
            try {
                colorPreview.setBackground(Color.decode((String) colorCombo.getSelectedItem()));
            } catch (Exception ex) {
                // ignore
            }
        });

        panel.add(new JLabel("Название:"));
        panel.add(nameField);
        panel.add(new JLabel("Тип:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Цвет:"));
        panel.add(colorCombo);
        panel.add(new JLabel("Превью:"));
        panel.add(colorPreview);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Добавить категорию",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите название категории",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Category category = new Category();
            category.setName(name);
            category.setType(typeCombo.getSelectedIndex() == 0 ? CategoryType.INCOME : CategoryType.EXPENSE);
            category.setColor((String) colorCombo.getSelectedItem());

            if (financeService.addCategory(category)) {
                JOptionPane.showMessageDialog(this, "Категория добавлена",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении категории",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCategory(ActionEvent e) {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите категорию для удаления",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long categoryId = (Long) tableModel.getValueAt(selectedRow, 0);
        String categoryName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить категорию '" + categoryName + "'?\n" +
                        "Все транзакции в этой категории будут без категории.",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (financeService.deleteCategory(categoryId)) {
                JOptionPane.showMessageDialog(this,
                        "Категория удалена",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при удалении категории",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Кастомный рендерер для отображения цвета
    private class ColorCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel colorPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (value != null && value instanceof String) {
                        try {
                            g.setColor(Color.decode((String) value));
                            g.fillRect(5, 5, getWidth() - 10, getHeight() - 10);
                            g.setColor(Color.BLACK);
                            g.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
                        } catch (Exception e) {
                            // Если не удалось декодировать цвет, показываем текст
                            g.setColor(Color.BLACK);
                            g.drawString(value.toString(), 10, getHeight() / 2 + 5);
                        }
                    }
                }
            };

            if (isSelected) {
                colorPanel.setBackground(table.getSelectionBackground());
            } else {
                colorPanel.setBackground(table.getBackground());
            }

            return colorPanel;
        }
    }
}