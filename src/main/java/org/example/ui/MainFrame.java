package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("Financy - Учет личных финансов");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
        setupWindowListener();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Создаем панели для разных функций
        tabbedPane.addTab("📊 Дашборд", new DashboardPanel());
        tabbedPane.addTab("💳 Транзакции", new TransactionPanel());
        tabbedPane.addTab("🗂️ Категории", new CategoryPanel());
        tabbedPane.addTab("📋 Отчеты", new ReportPanel());  // Добавляем новую вкладку

        add(tabbedPane, BorderLayout.CENTER);

        // Панель статуса
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel(" Готово");
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Кнопка выхода
        JButton exitButton = new JButton("Выход");
        exitButton.addActionListener(e -> confirmExit());
        statusPanel.add(exitButton, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupWindowListener() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmExit();
            }
        });
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Вы уверены, что хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}