package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserWallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String walletName;
    private final List<ExpensesCategory> expensesCategories;
    private final List<IncomeCategory> incomeCategories;
    private double balance = 0;

    public UserWallet(String walletName){
        this.walletName = walletName;
        expensesCategories = new ArrayList<>();
        incomeCategories = new ArrayList<>();
    }

    public String getName(){
        return walletName;
    }

    public double getBalance(){
        return balance;
    }

    public void registerExpenses(String categoryName, double expenses){
        var storedCategory = expensesCategories.stream().filter(c -> c.getName().equals(categoryName)).findFirst();
        if(storedCategory.isPresent()) {
            var category = storedCategory.get();
            category.registerExpenses(expenses);
            if (category.checkLimitExceeded()) System.out.println("ВНИМАНИЕ: превышен бюджет по категории " + categoryName);
        }
        else{
            expensesCategories.add(new ExpensesCategory(categoryName, expenses));
        }

        balance -= expenses;
        if (balance < 0){
            System.out.println("ВНИМАНИЕ: расходы превышают доходы");
        }
    }

    public void registerIncome(String category, double income){
        var storedCategory = incomeCategories.stream().filter(c -> c.getName().equals(category)).findFirst();
        if(storedCategory.isPresent()) storedCategory.get().registerIncome(income);
        else incomeCategories.add(new IncomeCategory(category, income));

        balance += income;
    }

    public void setLimitForCategory(String categoryName, double limit){
        var storedCategory = expensesCategories.stream().filter(c -> c.getName().equals(categoryName)).findFirst();
        if(storedCategory.isPresent()) {
            storedCategory.get().setLimit(limit);
            if (storedCategory.get().checkLimitExceeded()) System.out.println("ВНИМАНИЕ: Превышен бюджет по категории " + categoryName);
        }
        else expensesCategories.add(new ExpensesCategory(categoryName, 0d, limit));
    }

    public List<ExpensesCategory> getExpensesCategories(){
        return expensesCategories;
    }

    public List<IncomeCategory> getIncomeCategories(){
        return incomeCategories;
    }

}
