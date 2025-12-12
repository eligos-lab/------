package org.example.ui;

import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.service.FinanceService;
import org.example.ui.dialogs.AddTransactionDialog;
import org.example.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionPanel extends JPanel {
    private FinanceService financeService;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JLabel summaryLabel;

    public TransactionPanel() {
        this.financeService = new FinanceService();
        initComponents();
        loadTransactions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель с кнопками и статистикой
        JPanel topPanel = new JPanel(new BorderLayout());

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addButton = new JButton("➕ Добавить");
        deleteButton = new JButton("🗑️ Удалить");
        refreshButton = new JButton("🔄 Обновить");

        addButton.addActionListener(e -> addTransaction());
        deleteButton.addActionListener(e -> deleteTransaction());
        refreshButton.addActionListener(e -> loadTransactions());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        topPanel.add(buttonPanel, BorderLayout.WEST);

        // Панель статистики
        summaryLabel = new JLabel("Всего транзакций: 0");
        summaryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(summaryLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Таблица транзакций
        String[] columns = {"ID", "Дата", "Тип", "Сумма", "Категория", "Описание"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Long.class;
                if (columnIndex == 3) return BigDecimal.class;
                return String.class;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(25);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(50); // ID
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Дата
        transactionTable.getColumnModel().getColumn(2).setMaxWidth(80); // Тип
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Сумма
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Категория
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(250); // Описание

        // Рендерер для цветового кодирования
        transactionTable.setDefaultRenderer(Object.class, new TransactionRenderer());

        // Рендерер для форматирования суммы
        transactionTable.getColumnModel().getColumn(3).setCellRenderer(new AmountRenderer());

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список транзакций"));

        add(scrollPane, BorderLayout.CENTER);

        // Информационная панель внизу
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("💡 Для редактирования транзакции удалите и создайте заново"));
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void loadTransactions() {
        try {
            tableModel.setRowCount(0);
            List<Transaction> transactions = financeService.getAllTransactions();

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);

            for (Transaction transaction : transactions) {
                Object[] row = {
                        transaction.getId(),
                        DateUtil.formatDateTime(transaction.getDate()),
                        transaction.getType().getDisplayName(),
                        transaction.getAmount(),
                        transaction.getCategory() != null ? transaction.getCategory().getName() : "-",
                        transaction.getDescription() != null ? transaction.getDescription() : ""
                };
                tableModel.addRow(row);
            }

            summaryLabel.setText("Всего транзакций: " + transactions.size());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Ошибка при загрузке транзакций: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTransaction() {
        AddTransactionDialog dialog = new AddTransactionDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                financeService
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadTransactions();
            // Обновляем главное окно для обновления дашборда
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame instanceof MainFrame) {
                MainFrame mainFrame = (MainFrame) frame;
                // Переключаемся на дашборд чтобы увидеть обновления
                mainFrame.getContentPane().revalidate();
                mainFrame.getContentPane().repaint();
            }
        }
    }

    private void deleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите транзакцию для удаления",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long transactionId = (Long) tableModel.getValueAt(selectedRow, 0);
        String transactionDate = (String) tableModel.getValueAt(selectedRow, 1);
        String transactionAmount = tableModel.getValueAt(selectedRow, 3).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Вы уверены, что хотите удалить транзакцию?<br>" +
                        "Дата: " + transactionDate + "<br>" +
                        "Сумма: " + transactionAmount + " ₽</html>",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (financeService.deleteTransaction(transactionId)) {
                JOptionPane.showMessageDialog(this,
                        "Транзакция удалена",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                loadTransactions();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при удалении",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Кастомный рендерер для цветового кодирования транзакций
    private class TransactionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (!isSelected) {
                String type = (String) tableModel.getValueAt(row, 2);

                if (type.equals(TransactionType.INCOME.getDisplayName())) {
                    c.setForeground(new Color(0, 100, 0)); // Темно-зеленый для доходов
                    c.setBackground(new Color(220, 255, 220)); // Светло-зеленый фон
                } else if (type.equals(TransactionType.EXPENSE.getDisplayName())) {
                    c.setForeground(new Color(150, 0, 0)); // Темно-красный для расходов
                    c.setBackground(new Color(255, 220, 220)); // Светло-красный фон
                } else {
                    c.setForeground(Color.BLACK);
                    c.setBackground(table.getBackground());
                }
            }

            return c;
        }
    }

    // Рендерер для форматирования суммы
    private class AmountRenderer extends DefaultTableCellRenderer {
        private NumberFormat formatter;

        public AmountRenderer() {
            formatter = NumberFormat.getNumberInstance(Locale.US);
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            if (value instanceof BigDecimal) {
                setText(formatter.format(value) + " ₽");
            } else {
                super.setValue(value);
            }
        }
    }
}