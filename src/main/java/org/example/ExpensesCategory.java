package org.example;

public class ExpensesCategory extends  AbstractCategory{

    private Double expenses = 0d;
    private Double limit = null;

    public ExpensesCategory(String name, Double expenses){
        super(name);
        this.expenses += expenses;
    }

    public ExpensesCategory(String name, Double expenses, Double limit){
        super(name);
        this.expenses += expenses;
        this.limit = limit;
    }
    public String toString(){
        if(limit != null){
            return String.format("%s: %.2f, Оставшийся бюджет: %f%n", this.getName(), expenses, limit-expenses);
        }
        return String.format("%s: %.2f%n", this.getName(), expenses);
    }

    public double getExpenses() {
        return expenses;
    }

    public void setLimit(Double limit){
        this.limit = limit;
    }

    public void registerExpenses(Double expenses){
        this.expenses += expenses;
    }

    public boolean checkLimitExceeded(){
        if(limit == null) return false;
        return expenses > limit;
    }
}
