package org.example.service;

import org.example.dao.TransactionDAO;
import org.example.dao.CategoryDAO;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.Category;
import org.example.model.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FinanceService {
    private TransactionDAO transactionDAO;
    private CategoryDAO categoryDAO;

    public FinanceService() {
        this.transactionDAO = new TransactionDAO();
        this.categoryDAO = new CategoryDAO();
    }

    public boolean addTransaction(Transaction transaction) {
        return transactionDAO.addTransaction(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactionDAO.getTransactionsByDateRange(start, end);
    }

    public List<Transaction> getTransactionsForCurrentMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        return transactionDAO.getTransactionsByDateRange(start, end);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public List<Category> getIncomeCategories() {
        return categoryDAO.getCategoriesByType(CategoryType.INCOME);
    }

    public List<Category> getExpenseCategories() {
        return categoryDAO.getCategoriesByType(CategoryType.EXPENSE);
    }

    public BigDecimal getTotalIncome() {
        return transactionDAO.getTotalIncome();
    }

    public BigDecimal getTotalExpense() {
        return transactionDAO.getTotalExpense();
    }

    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    public Map<String, BigDecimal> getExpensesByCategory() {
        Map<String, BigDecimal> result = new HashMap<>();
        List<Transaction> transactions = getAllTransactions();

        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.EXPENSE &&
                    transaction.getCategory() != null) {
                String categoryName = transaction.getCategory().getName();
                BigDecimal amount = result.getOrDefault(categoryName, BigDecimal.ZERO);
                result.put(categoryName, amount.add(transaction.getAmount()));
            }
        }

        return result;
    }

    public boolean deleteTransaction(Long id) {
        return transactionDAO.deleteTransaction(id);
    }

    public boolean addCategory(Category category) {
        return categoryDAO.addCategory(category);
    }

    public boolean deleteCategory(Long id) {
        return categoryDAO.deleteCategory(id);
    }

    public BigDecimal getIncomeForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = getTransactionsByDateRange(start, end);
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getExpenseForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = getTransactionsByDateRange(start, end);
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}