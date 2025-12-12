package org.example.model;

public enum TransactionType {
    INCOME("Доход"),
    EXPENSE("Расход");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}