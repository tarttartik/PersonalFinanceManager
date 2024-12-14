package org.example;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FinanceManager {

    private final ArrayList<User> users;
    private User currentUser = null;
    private UserWallet currentWallet = null;
    private final SaveService saveService = new SaveService("src/main/resources/save.txt");

    public FinanceManager(){
        users = saveService.getSavedUserData();
    }

    public void saveChanges(){
        saveService.saveUserData(users);
    }

    public boolean tryRegisterUser(String login, String password){
        if (users.stream().anyMatch(u -> u.getLogin().equals(login))) return false;
        var user = new User(login, password);
        users.add(user);
        currentUser = user;
        return true;
    }

    public boolean tryLogInUser(String login, String password){
        var user = users.stream().filter(u -> u.getLogin().equals(login)
                && u.getPassword().equals(password)).findAny();

        if(user.isEmpty()) return false;

        currentUser = user.get();
        return true;
    }

    public boolean tryAddWalletForUser(String walletName){
        checkCurrentUser();
        var wallets = currentUser.getAllWallets();
        if(currentUser.getAllWallets().stream().anyMatch(w -> w.getName().equals(walletName))) return false;
        currentUser.addWallet(walletName);
        return true;
    }

    public void showAllUserWalletsNames(){
        checkCurrentUser();
        var wallets = currentUser.getAllWallets().stream().map(UserWallet::getName).toList();
        System.out.print("Ваши кошельки: ");
        for(var wallet : wallets) System.out.print(wallet + " ");
    }

    public boolean setCurrentWallet(String walletName){
        checkCurrentUser();
        var wallet = currentUser.getWallet(walletName);
        if(wallet.isEmpty()){
            System.out.println("ОШИБКА: кошелька с таким названием не существует");
            return false;
        }
        currentWallet = wallet.get();
        return true;
    }

    public void showAllExpensesCategoriesNames(){
        checkCurrentWallet();
        var categories = currentWallet.getExpensesCategories().stream().map(ExpensesCategory::getName).toList();
        System.out.println("Ваши категории расходов: ");
        for(var category : categories) System.out.println(category);
    }

    public void showAllIncomeCategoriesNames(){
        checkCurrentWallet();
        var categories =  currentWallet.getIncomeCategories().stream().map(IncomeCategory::getName).toList();
        System.out.println("Ваши категории доходов: ");
        for(var category : categories) System.out.println(category);
    }

    public void showExpenses(@NotNull List<String> names){
        checkCurrentWallet();
        var targetCategories = new ArrayList<ExpensesCategory>();
        for(var catName : names){
            var cat = currentWallet.getExpensesCategories().stream().filter(c -> c.getName().equals(catName)).findFirst();
            if(cat.isEmpty()){
                System.out.printf("Категория %s не найдена!%n", catName);
                continue;
            }
            targetCategories.add(cat.get());
        }
        printExpenses(targetCategories);
    }

    public void showIncome(@NotNull List<String> names) {
        checkCurrentWallet();
        var targetCategories = new ArrayList<IncomeCategory>();
        for(var catName : names){
            var cat = currentWallet.getIncomeCategories().stream().filter(c -> c.getName().equals(catName)).findFirst();
            if(cat.isEmpty()){
                System.out.printf("Категория %s не найдена!%n", catName);
                continue;
            }
            targetCategories.add(cat.get());
        }
        printIncome(targetCategories);
    }

    public void showIncome(){
        checkCurrentWallet();
        printIncome(currentWallet.getIncomeCategories());
    }

    public void showExpenses(){
        checkCurrentWallet();
        printExpenses(currentWallet.getExpensesCategories());
    }

    public void showBalance(){
        checkCurrentWallet();
        System.out.println("Баланс: " + currentWallet.getBalance());
    }

    public void setLimitForCategory(String categoryName, double limit){
        checkCurrentWallet();
        currentWallet.setLimitForCategory(categoryName, limit);
    }

    public void registerExpenses(String categoryName, double expenses){
        checkCurrentWallet();
        currentWallet.registerExpenses(categoryName, expenses);
    }

    public void registerIncome(String categoryName, double income){
        checkCurrentWallet();
        currentWallet.registerIncome(categoryName, income);
    }

    public boolean tryExecuteTransferBetweenWallets(String wallet, double sum){
        checkCurrentUser();
        checkCurrentWallet();
        if(wallet.equals(currentWallet.getName())){
            System.out.println("ОШИБКА: Кошелёк не должен совпадать с текущим");
            return  false;
        }
        if(currentWallet.getBalance() < sum){
            System.out.println("ОШИБКА: Недостаточно средств");
            return false;
        }
        var targetWallet = currentUser.getWallet(wallet);
        if(targetWallet.isEmpty()){
            System.out.println("ОШИБКА: Неверное название кошелька");
            return false;
        }

        targetWallet.get().registerIncome("Переводы",sum);
        currentWallet.registerExpenses("Переводы", sum);
        return true;
    }

    public boolean tryExecuteTransferBetweenUsers(String userLogin, String wallet, double sum){
        checkCurrentWallet();
        checkCurrentUser();
        if(currentWallet.getBalance() < sum){
            System.out.println("ОШИБКА: Недостаточно средств");
            return false;
        }

        if(wallet.equals(currentWallet.getName()) && userLogin.equals(currentUser.getLogin())){
            System.out.println("ОШИБКА: Кошелёк не должен совпадать с текущим");
            return  false;
        }

        var targetUser = users.stream().filter(c -> c.getLogin().equals(userLogin)).findFirst();
        if(targetUser.isEmpty())
        {
            System.out.printf("ОШИБКА: Пользователь с логином %s не найден %n", userLogin);
            return false;
        }

        var targetWallet = targetUser.get().getWallet(wallet);
        if(targetWallet.isEmpty()){
            System.out.printf("ОШИБКА: У пользователя с логином %s не найден кошелёк с именем %s %n", userLogin, wallet);
            return false;
        }

        targetWallet.get().registerIncome("Переводы",sum);
        currentWallet.registerExpenses("Переводы", sum);
        return true;
    }

    public boolean tryDeleteWallet(@NotNull String walletName){
        checkCurrentUser();
        checkCurrentWallet();

        if(walletName.equals(currentWallet.getName())){
            System.out.println("ОШИБКА: Невозможно удалить кошелёк, с которым вы работаете. Пожалуйста, сначала перейдите в другой кошелёк");
            return false;
        }

        var wallet = currentUser.getWallet(walletName);
        if(wallet.isEmpty()){
            System.out.println("ОШИБКА: Не найден кошелёк " + walletName);
            return false;
        }

        currentUser.deleteWallet(wallet.get());
        return true;
    }

    private void checkCurrentUser(){
        if(currentUser != null) return;
        System.out.println("ОШИБКА: Пользователь не определён");
        throw new RuntimeException();
    }

    private void checkCurrentWallet(){
        if(currentWallet != null) return;
        System.out.println("ОШИБКА: Кошелёк не задан");
        throw new RuntimeException();
    }

    private void printIncome(@NotNull List<IncomeCategory> categories){
        System.out.println("Доходы по категориям:");
        for(var category : categories) System.out.print(category.toString());
        System.out.println("Всего: " + categories.stream().mapToDouble(IncomeCategory::getIncome).sum());
    }

    private void printExpenses(@NotNull List<ExpensesCategory> categories){
        System.out.println("Расходы по категориям:");
        for(var category : categories) System.out.print(category.toString());
        System.out.println("Всего: " + categories.stream().mapToDouble(ExpensesCategory::getExpenses).sum());
    }
}
