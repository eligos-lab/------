package org.example.ui.dialogs;

import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.Category;
import org.example.service.FinanceService;
import org.example.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AddTransactionDialog extends JDialog {
    private FinanceService financeService;
    private boolean saved = false;

    private JComboBox<TransactionType> typeComboBox;
    private JTextField amountField;
    private JComboBox<Category> categoryComboBox;
    private JTextField dateField;
    private JTextArea descriptionArea;

    public AddTransactionDialog(Frame parent, FinanceService financeService) {
        super(parent, "Добавить транзакцию", true);
        this.financeService = financeService;
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(400, 350);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Панель формы
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Тип транзакции
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Тип:"), gbc);

        gbc.gridx = 1;
        typeComboBox = new JComboBox<>(TransactionType.values());
        formPanel.add(typeComboBox, gbc);

        // Сумма
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Сумма (₽):"), gbc);

        gbc.gridx = 1;
        amountField = new JTextField(15);
        amountField.setToolTipText("Например: 1500.50");
        formPanel.add(amountField, gbc);

        // Категория
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Категория:"), gbc);

        gbc.gridx = 1;
        categoryComboBox = new JComboBox<>();
        updateCategories();
        formPanel.add(categoryComboBox, gbc);

        // Дата
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Дата:"), gbc);

        gbc.gridx = 1;
        dateField = new JTextField(DateUtil.formatDateTime(LocalDateTime.now()), 15);
        dateField.setToolTipText("Формат: дд.мм.гггг чч:мм");
        formPanel.add(dateField, gbc);

        // Описание
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Описание:"), gbc);

        gbc.gridx = 1;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        descriptionArea = new JTextArea(3, 15);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");

        saveButton.addActionListener(e -> saveTransaction());
        cancelButton.addActionListener(e -> dispose());

        // Назначаем клавиши по умолчанию
        getRootPane().setDefaultButton(saveButton);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Обработчик изменения типа транзакции
        typeComboBox.addActionListener(e -> updateCategories());
    }

    private void updateCategories() {
        TransactionType selectedType = (TransactionType) typeComboBox.getSelectedItem();
        categoryComboBox.removeAllItems();

        List<Category> categories;
        if (selectedType == TransactionType.INCOME) {
            categories = financeService.getIncomeCategories();
        } else {
            categories = financeService.getExpenseCategories();
        }

        // Добавляем пустую категорию
        categoryComboBox.addItem(null);

        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }

        if (!categories.isEmpty()) {
            categoryComboBox.setSelectedIndex(1); // Первая реальная категория
        }
    }

    private void saveTransaction() {
        try {
            // Валидация суммы
            if (amountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите сумму", "Ошибка", JOptionPane.ERROR_MESSAGE);
                amountField.requestFocus();
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText().trim().replace(",", "."));
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException("Сумма должна быть положительной");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Некорректная сумма. Используйте числа, например: 1500.50",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                amountField.requestFocus();
                amountField.selectAll();
                return;
            }

            TransactionType type = (TransactionType) typeComboBox.getSelectedItem();
            Category category = (Category) categoryComboBox.getSelectedItem();

            // Парсинг даты
            LocalDateTime date;
            try {
                date = DateUtil.parseDateTime(dateField.getText());
                if (date == null) {
                    date = LocalDateTime.now();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Некорректная дата. Используйте формат: дд.мм.гггг чч:мм",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                dateField.requestFocus();
                dateField.selectAll();
                return;
            }

            String description = descriptionArea.getText().trim();

            // Создание транзакции
            Transaction transaction = new Transaction();
            transaction.setType(type);
            transaction.setAmount(amount);
            transaction.setCategory(category);
            transaction.setDate(date);
            transaction.setDescription(description.isEmpty() ? null : description);

            // Сохранение
            if (financeService.addTransaction(transaction)) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при сохранении транзакции",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}