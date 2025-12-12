package org.example.ui;

import org.example.service.FinanceService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private FinanceService financeService;

    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel balanceLabel;
    private JPanel chartPanelContainer;

    public DashboardPanel() {
        this.financeService = new FinanceService();
        initComponents();
        updateData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Панель статистики
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Финансовая статистика"));

        // Создаем карточки для статистики
        totalIncomeLabel = new JLabel("0 ₽", SwingConstants.CENTER);
        totalIncomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalIncomeLabel.setForeground(new Color(0, 150, 0));

        totalExpenseLabel = new JLabel("0 ₽", SwingConstants.CENTER);
        totalExpenseLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalExpenseLabel.setForeground(new Color(200, 0, 0));

        balanceLabel = new JLabel("0 ₽", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        balanceLabel.setForeground(new Color(0, 100, 200));

        statsPanel.add(createStatCard("Доходы", totalIncomeLabel, new Color(220, 255, 220)));
        statsPanel.add(createStatCard("Расходы", totalExpenseLabel, new Color(255, 220, 220)));
        statsPanel.add(createStatCard("Баланс", balanceLabel, new Color(220, 230, 255)));

        add(statsPanel, BorderLayout.NORTH);

        // Панель для диаграммы
        chartPanelContainer = new JPanel(new BorderLayout());
        chartPanelContainer.setBorder(BorderFactory.createTitledBorder("Расходы по категориям"));
        add(chartPanelContainer, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Обновить данные");
        refreshButton.addActionListener(e -> updateData());
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(bgColor);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.DARK_GRAY);

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void updateData() {
        try {
            // Обновление статистики
            BigDecimal income = financeService.getTotalIncome();
            BigDecimal expense = financeService.getTotalExpense();
            BigDecimal balance = financeService.getBalance();

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);

            totalIncomeLabel.setText(formatter.format(income) + " ₽");
            totalExpenseLabel.setText(formatter.format(expense) + " ₽");
            balanceLabel.setText(formatter.format(balance) + " ₽");

            // Обновление диаграммы
            updateChart();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Ошибка при обновлении данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateChart() {
        try {
            Map<String, BigDecimal> expensesByCategory = financeService.getExpensesByCategory();

            if (expensesByCategory.isEmpty()) {
                chartPanelContainer.removeAll();
                chartPanelContainer.add(new JLabel("Нет данных о расходах", SwingConstants.CENTER), BorderLayout.CENTER);
                chartPanelContainer.revalidate();
                chartPanelContainer.repaint();
                return;
            }

            DefaultPieDataset dataset = new DefaultPieDataset();
            double total = 0;

            // Собираем данные и считаем общую сумму
            for (Map.Entry<String, BigDecimal> entry : expensesByCategory.entrySet()) {
                double value = entry.getValue().doubleValue();
                dataset.setValue(entry.getKey(), value);
                total += value;
            }

            // Если общая сумма слишком мала, не показываем диаграмму
            if (total < 0.01) {
                chartPanelContainer.removeAll();
                chartPanelContainer.add(new JLabel("Сумма расходов слишком мала для отображения",
                        SwingConstants.CENTER), BorderLayout.CENTER);
                chartPanelContainer.revalidate();
                chartPanelContainer.repaint();
                return;
            }

            // Создаем диаграмму
            JFreeChart chart = ChartFactory.createPieChart(
                    "Расходы по категориям (всего: " + String.format("%.2f", total) + " ₽)",
                    dataset,
                    true,  // легенда
                    true,  // подсказки
                    false  // URLs
            );

            // Настраиваем внешний вид
            chart.setBackgroundPaint(Color.WHITE);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(500, 300));

            chartPanelContainer.removeAll();
            chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            chartPanelContainer.removeAll();
            chartPanelContainer.add(new JLabel("Ошибка при создании диаграммы: " + e.getMessage(),
                    SwingConstants.CENTER), BorderLayout.CENTER);
            chartPanelContainer.revalidate();
            chartPanelContainer.repaint();
        }
    }
}