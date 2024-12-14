package org.example;

public class IncomeCategory  extends  AbstractCategory{
    private Double income = 0d;

    public IncomeCategory(String name, double income){
        super(name);
        this.income = income;
    }

    public double getIncome(){
        return income;
    }

    public void registerIncome(Double income){
        this.income += income;
    }

    public String toString(){
        return String.format("%s: %2f%n", this.getName(), income);
    }
}
