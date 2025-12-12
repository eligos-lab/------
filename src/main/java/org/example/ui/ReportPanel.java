package org.example.ui;

import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.service.FinanceService;
import org.example.util.DateUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ReportPanel extends JPanel {
    private FinanceService financeService;

    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;
    private JButton generateButton;
    private JButton exportButton;

    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel balanceLabel;
    private JLabel periodLabel;

    private JTable reportTable;
    private DefaultTableModel tableModel;

    private JPanel chartPanelContainer;

    private LocalDate fromDate;
    private LocalDate toDate;

    public ReportPanel() {
        this.financeService = new FinanceService();
        initComponents();
        setDefaultDates();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Верхняя панель с выбором дат
        add(createDatePanel(), BorderLayout.NORTH);

        // Центральная панель с результатами
        add(createResultsPanel(), BorderLayout.CENTER);
    }

    private JPanel createDatePanel() {
        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder("Выбор периода отчета"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Дата "С"
        gbc.gridx = 0;
        gbc.gridy = 0;
        datePanel.add(new JLabel("С:"), gbc);

        gbc.gridx = 1;
        fromDateSpinner = createDateSpinner();
        datePanel.add(fromDateSpinner, gbc);

        // Дата "По"
        gbc.gridx = 0;
        gbc.gridy = 1;
        datePanel.add(new JLabel("По:"), gbc);

        gbc.gridx = 1;
        toDateSpinner = createDateSpinner();
        datePanel.add(toDateSpinner, gbc);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        generateButton = new JButton("📊 Сформировать отчет");
        generateButton.addActionListener(e -> generateReport());

        exportButton = new JButton("💾 Экспорт в CSV");
        exportButton.addActionListener(e -> exportToCSV());
        exportButton.setEnabled(false);

        buttonPanel.add(generateButton);
        buttonPanel.add(exportButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        datePanel.add(buttonPanel, gbc);

        return datePanel;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));

        // Панель статистики
        resultsPanel.add(createStatsPanel(), BorderLayout.NORTH);

        // Таблица с транзакциями
        resultsPanel.add(createTablePanel(), BorderLayout.CENTER);

        // Диаграмма
        resultsPanel.add(createChartPanel(), BorderLayout.SOUTH);

        return resultsPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Статистика за период"));

        // Период
        JPanel periodCard = createStatCard("Период", "");
        periodLabel = new JLabel("Не выбран", SwingConstants.CENTER);
        periodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        periodCard.add(periodLabel);
        statsPanel.add(periodCard);

        // Доходы
        JPanel incomeCard = createStatCard("Доходы", "0 ₽");
        totalIncomeLabel = new JLabel("0 ₽", SwingConstants.CENTER);
        totalIncomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalIncomeLabel.setForeground(new Color(0, 150, 0));
        incomeCard.add(totalIncomeLabel);
        statsPanel.add(incomeCard);

        // Расходы
        JPanel expenseCard = createStatCard("Расходы", "0 ₽");
        totalExpenseLabel = new JLabel("0 ₽", SwingConstants.CENTER);
        totalExpenseLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalExpenseLabel.setForeground(new Color(200, 0, 0));
        expenseCard.add(totalExpenseLabel);
        statsPanel.add(expenseCard);

        // Баланс
        JPanel balanceCard = createStatCard("Баланс", "0 ₽");
        balanceLabel = new JLabel("0 ₽", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setForeground(new Color(0, 100, 200));
        balanceCard.add(balanceLabel);
        statsPanel.add(balanceCard);

        return statsPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Транзакции за период"));

        String[] columns = {"Дата", "Тип", "Сумма", "Категория", "Описание"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return BigDecimal.class;
                return String.class;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setRowHeight(25);
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        reportTable.getColumnModel().getColumn(4).setPreferredWidth(250);

        // Рендерер для цветового кодирования
        reportTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String type = (String) tableModel.getValueAt(row, 1);

                    if ("Доход".equals(type)) {
                        c.setForeground(new Color(0, 100, 0));
                        c.setBackground(new Color(220, 255, 220));
                    } else if ("Расход".equals(type)) {
                        c.setForeground(new Color(150, 0, 0));
                        c.setBackground(new Color(255, 220, 220));
                    } else {
                        c.setForeground(Color.BLACK);
                        c.setBackground(table.getBackground());
                    }
                }

                return c;
            }
        });

        // Рендерер для суммы
        reportTable.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);

            {
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
        });

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setPreferredSize(new Dimension(800, 200));

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Панель с количеством транзакций
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel countLabel = new JLabel("Всего транзакций: 0");
        countPanel.add(countLabel);
        tablePanel.add(countPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private JPanel createChartPanel() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Динамика доходов и расходов"));
        chartPanel.setPreferredSize(new Dimension(800, 300));

        chartPanelContainer = new JPanel(new BorderLayout());
        chartPanelContainer.add(new JLabel("Сформируйте отчет для отображения диаграммы",
                SwingConstants.CENTER), BorderLayout.CENTER);

        chartPanel.add(chartPanelContainer, BorderLayout.CENTER);
        return chartPanel;
    }

    private JPanel createStatCard(String title, String defaultValue) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(Color.DARK_GRAY);

        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd.MM.yyyy");
        spinner.setEditor(editor);
        spinner.setValue(new java.util.Date());

        // Устанавливаем минимальную ширину
        spinner.setPreferredSize(new Dimension(120, 25));

        return spinner;
    }

    private void setDefaultDates() {
        // По умолчанию - текущий месяц
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        fromDateSpinner.setValue(java.sql.Date.valueOf(firstDayOfMonth));
        toDateSpinner.setValue(java.sql.Date.valueOf(lastDayOfMonth));
    }

    private void generateReport() {
        try {
            // Получаем даты из спиннеров
            java.util.Date fromUtilDate = (java.util.Date) fromDateSpinner.getValue();
            java.util.Date toUtilDate = (java.util.Date) toDateSpinner.getValue();

            fromDate = new java.sql.Date(fromUtilDate.getTime()).toLocalDate();
            toDate = new java.sql.Date(toUtilDate.getTime()).toLocalDate();

            // Проверяем, что дата "С" раньше даты "По"
            if (fromDate.isAfter(toDate)) {
                JOptionPane.showMessageDialog(this,
                        "Дата 'С' должна быть раньше даты 'По'",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Получаем транзакции за период
            LocalDateTime startDateTime = fromDate.atStartOfDay();
            LocalDateTime endDateTime = toDate.atTime(23, 59, 59);

            List<Transaction> transactions = financeService.getTransactionsByDateRange(
                    startDateTime, endDateTime);

            // Обновляем статистику
            updateStatistics(transactions);

            // Обновляем таблицу
            updateTable(transactions);

            // Обновляем диаграмму
            updateChart(transactions);

            // Активируем кнопку экспорта
            exportButton.setEnabled(!transactions.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Ошибка при формировании отчета: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatistics(List<Transaction> transactions) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else {
                totalExpense = totalExpense.add(transaction.getAmount());
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Форматируем числа
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);

        // Обновляем метки
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        periodLabel.setText(fromDate.format(dateFormatter) + " - " + toDate.format(dateFormatter));
        totalIncomeLabel.setText(formatter.format(totalIncome) + " ₽");
        totalExpenseLabel.setText(formatter.format(totalExpense) + " ₽");
        balanceLabel.setText(formatter.format(balance) + " ₽");
    }

    private void updateTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);

        for (Transaction transaction : transactions) {
            Object[] row = {
                    DateUtil.formatDateTime(transaction.getDate()),
                    transaction.getType().getDisplayName(),
                    transaction.getAmount(),
                    transaction.getCategory() != null ? transaction.getCategory().getName() : "-",
                    transaction.getDescription() != null ? transaction.getDescription() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void updateChart(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            chartPanelContainer.removeAll();
            chartPanelContainer.add(new JLabel("Нет данных для построения диаграммы",
                    SwingConstants.CENTER), BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();
            return;
        }

        try {
            // Группируем по дням
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Создаем мапы для группировки
            java.util.Map<LocalDate, BigDecimal> incomeByDate = new java.util.HashMap<>();
            java.util.Map<LocalDate, BigDecimal> expenseByDate = new java.util.HashMap<>();

            for (Transaction transaction : transactions) {
                LocalDate date = transaction.getDate().toLocalDate();
                BigDecimal amount = transaction.getAmount();

                if (transaction.getType() == TransactionType.INCOME) {
                    BigDecimal current = incomeByDate.getOrDefault(date, BigDecimal.ZERO);
                    incomeByDate.put(date, current.add(amount));
                } else {
                    BigDecimal current = expenseByDate.getOrDefault(date, BigDecimal.ZERO);
                    expenseByDate.put(date, current.add(amount));
                }
            }

            // Добавляем данные в dataset
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM");

            // Сортируем даты
            java.util.List<LocalDate> dates = new java.util.ArrayList<>(
                    new java.util.HashSet<>(incomeByDate.keySet()));
            dates.addAll(expenseByDate.keySet());
            dates = dates.stream().distinct().sorted().collect(java.util.stream.Collectors.toList());

            for (LocalDate date : dates) {
                String dateStr = date.format(dateFormatter);
                BigDecimal income = incomeByDate.getOrDefault(date, BigDecimal.ZERO);
                BigDecimal expense = expenseByDate.getOrDefault(date, BigDecimal.ZERO);

                if (income.compareTo(BigDecimal.ZERO) > 0) {
                    dataset.addValue(income.doubleValue(), "Доходы", dateStr);
                }
                if (expense.compareTo(BigDecimal.ZERO) > 0) {
                    dataset.addValue(expense.doubleValue(), "Расходы", dateStr);
                }
            }

            // Создаем диаграмму
            JFreeChart chart = ChartFactory.createBarChart(
                    "Динамика доходов и расходов по дням",
                    "Дата",
                    "Сумма (₽)",
                    dataset
            );

            chart.setBackgroundPaint(Color.WHITE);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(700, 250));

            chartPanelContainer.removeAll();
            chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            chartPanelContainer.removeAll();
            chartPanelContainer.add(new JLabel("Ошибка при построении диаграммы: " + e.getMessage(),
                    SwingConstants.CENTER), BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();
        }
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Экспорт отчета в CSV");
        fileChooser.setSelectedFile(new java.io.File("financy_report_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
                // Заголовок
                writer.println("Отчет Financy");
                writer.println("Период: " + periodLabel.getText());
                writer.println("Доходы: " + totalIncomeLabel.getText());
                writer.println("Расходы: " + totalExpenseLabel.getText());
                writer.println("Баланс: " + balanceLabel.getText());
                writer.println();

                // Заголовки таблицы
                writer.println("Дата;Тип;Сумма;Категория;Описание");

                // Данные
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    writer.println(
                            tableModel.getValueAt(i, 0) + ";" +
                                    tableModel.getValueAt(i, 1) + ";" +
                                    tableModel.getValueAt(i, 2) + ";" +
                                    tableModel.getValueAt(i, 3) + ";" +
                                    tableModel.getValueAt(i, 4)
                    );
                }

                JOptionPane.showMessageDialog(this,
                        "Отчет успешно экспортирован в файл:\n" + file.getAbsolutePath(),
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Ошибка при экспорте: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}