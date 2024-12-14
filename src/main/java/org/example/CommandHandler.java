package org.example;

import java.util.Arrays;
import java.util.Scanner;

public class CommandHandler {
    private final FinanceManager financeManager;
    private final Scanner scanner;

    public CommandHandler(){
        financeManager = new FinanceManager();
        scanner = new Scanner(System.in);
    }

    public void run(){
        System.out.printf("Здравствуйте! У вас уже есть аккаунт?%n" +
                "Чтобы войти, введите команду %s. Чтобы зарегистрироваться, введите %s.%n",
                UserCommand.LOG_IN, UserCommand.REGISTER);
        try{
            if (!tryDetermineUser()) run();
            printCommandMenu();
            System.out.printf("Подсказка: введите %s в любой момент, чтобы прервать текущее действие и вернуться в меню команд. %n",
                    UserCommand.PRINT_MENU);
            System.out.printf("Пожалуйcта, всегда используйте команду %s для выхода из приложения. Это обеспечит сохранение данных.%n",
                    UserCommand.QUIT);
            handleUserCommand();

        }
        catch(Exception e){
            financeManager.saveChanges();
            throw e;
        }
    }

    private boolean tryDetermineUser(){
        var command = parseInput();
        switch (command){
            case UserCommand.REGISTER:
                if (!tryHandleRegistration()) return false;
                handleCreateWallet(true);
                return true;
            case UserCommand.LOG_IN:
                if(!tryHandleLogIn()) return false;
                determineWallet();
                return true;
            default:
                System.out.printf("Вы ввели неверную команду! Пожалуйста, введите %s или %s.",
                        UserCommand.LOG_IN, UserCommand.REGISTER);
                return tryDetermineUser();
        }
    }

    private void determineWallet(){
        System.out.println("Пожалуйста, выберите кошелёк.");
        financeManager.showAllUserWalletsNames();
        System.out.printf("%nЕсли вы хотите создать новый кошелёк, введите %s%n", UserCommand.CREATE_WALLET);
        var command = parseInput();
        if(command.equals(UserCommand.CREATE_WALLET)){
            handleCreateWallet(false);
            determineWallet();
        }
        else if(!financeManager.setCurrentWallet(command))
        {
            System.out.println("Пожалуйста, повторите попытку.");
            determineWallet();
        }
    }

    private void printCommandMenu(){
        System.out.println("Доступные команды: ");
        System.out.printf("* Узнать баланс: %s%n", UserCommand.GET_BALANCE);
        System.out.printf("* Внести доходы: %s%n", UserCommand.REGISTER_INCOME);
        System.out.printf("* Внести расходы: %s%n", UserCommand.REGISTER_EXPENSES);
        System.out.printf("* Получить отчёт о доходах: %s%n", UserCommand.GET_INCOME_REPORT);
        System.out.printf("* Получить отчёт о расходах: %s%n", UserCommand.GET_EXPENSES_REPORT);
        System.out.printf("* Получить полный отчёт: %s%n", UserCommand.GET_FULL_REPORT);
        System.out.printf("* Добавить бюджет для категории: %s%n", UserCommand.SET_CATEGORY_BUDGET);
        System.out.printf("* Перевести средства на другой кошелёк: %s%n", UserCommand.WALLET_TRANSFER);
        System.out.printf("* Перевести средства другому пользователю: %s%n", UserCommand.USER_TRANSFER);
        System.out.printf("* Создать новый кошелёк: %s%n", UserCommand.CREATE_WALLET);
        System.out.printf("* Удалить один из кошельков: %s%n", UserCommand.DELETE_WALLET);
        System.out.printf("* Сменить кошелёк: %s%n", UserCommand.CHANGE_WALLET);
        System.out.printf("* Выйти из аккаунта: %s%n", UserCommand.LOG_OUT);
    }

    private void handleUserCommand(){
        while(true){
            System.out.println("Введите команду (menu - посмотреть доступные команды): ");
            var command = parseInput();
            switch(command){
                case UserCommand.GET_BALANCE -> financeManager.showBalance();
                case UserCommand.REGISTER_INCOME -> handleRegisterIncome();
                case UserCommand.REGISTER_EXPENSES -> handleRegisterExpenses();
                case UserCommand.GET_FULL_REPORT -> handleGetFullReport();
                case UserCommand.GET_INCOME_REPORT -> handleGetIncomeReport();
                case UserCommand.GET_EXPENSES_REPORT -> handleGetExpensesReport();
                case UserCommand.SET_CATEGORY_BUDGET -> handleSetCategoryLimit();
                case UserCommand.CHANGE_WALLET -> handleChangeWallet();
                case UserCommand.CREATE_WALLET -> handleCreateWallet(false);
                case UserCommand.WALLET_TRANSFER -> handleTransferBetweenWallets();
                case UserCommand.USER_TRANSFER -> handleTransferBetweenUsers();
                case UserCommand.DELETE_WALLET -> handleDeleteWallet();
                case UserCommand.LOG_OUT -> {
                    financeManager.saveChanges();
                    run();
                }
                case UserCommand.QUIT -> {
                    financeManager.saveChanges();
                    System.exit(0);
                }
                default -> {
                    System.out.println("Неверная команда!");
                    continue;
                }
            }
            System.out.println("Операция успешно выполнена");
        }
    }

    private boolean tryHandleRegistration(){
        var attempts = 3;
        while(attempts > 0) {
            System.out.println("Введите логин: ");
            var login = parseInput();
            System.out.println("Введите пароль: ");
            var password = parseInput();
            if (financeManager.tryRegisterUser(login, password))
            {
                System.out.println("Регистрация успешно завершена");
                return true;
            }
            else{
                attempts--;
                System.out.println("Попытка регистрации не удалась: пользователь с таким логином уже существует " +
                        "Осталось попыток: " + attempts);
            }
        }
        return false;
    }

    private boolean tryHandleLogIn(){
        var attempts = 3;
        while(attempts > 0) {
            System.out.println("Введите логин: ");
            var login = parseInput();
            System.out.println("Введите пароль: ");
            var password = parseInput();

            if(financeManager.tryLogInUser(login, password)){
                System.out.printf("Здравствуйте, %s!%n", login);
                return true;
            }
            else {
                attempts--;
                System.out.println("Попытка входа не удалась. Осталось попыток: " + attempts);
            }
        }
        return false;
    }

    private void handleCreateWallet(boolean newUser){
        System.out.println("Введите название нового кошелька: ");
        var walletName = parseInput();
        if (financeManager.tryAddWalletForUser(walletName))
        {
            System.out.println("Кошелёк успешно создан");
            if(newUser){
                financeManager.setCurrentWallet(walletName);
            }
            return;
        }

        System.out.println("ОШИБКА: Кошелёк с таким названием уже существует! Повторите попытку");
        handleCreateWallet(newUser);
    }

    private void handleRegisterIncome(){
        System.out.println("Введите категорию доходов: ");
        var category = parseInput();
        System.out.println("Введите сумму: ");
        var income = 0d;
        try {
            income = Double.parseDouble(parseInput());
            financeManager.registerIncome(category, income);
        }
        catch(Exception  ex){
            System.out.println("Не удалось прочитать сумму. Пожалйуста, попробуйте снова");
            handleRegisterIncome();
        }
    }

    private void handleRegisterExpenses(){
        System.out.println("Введите категорию расходов: ");
        var category = parseInput();
        System.out.println("Введите сумму: ");
        var income = 0d;
        try {
            income = Double.parseDouble(parseInput());
            financeManager.registerExpenses(category, income);
        }
        catch(Exception  ex){
            System.out.println("ОШИБКА: Не удалось прочитать сумму. Пожалуйста, попробуйте снова");
            handleRegisterExpenses();
        }
    }

    private void handleGetFullReport(){
        financeManager.showBalance();
        System.out.println();
        financeManager.showIncome();
        System.out.println();
        financeManager.showExpenses();
    }

    private void handleGetIncomeReport(){
        System.out.printf("Выберите категории доходов для отчёта (введите названия через пробел).%n" +
                "Если хотите получить отчёт по всем категориям, введите команду %s%n", UserCommand.ALL_CATEGORIES);
        financeManager.showAllIncomeCategoriesNames();
        var command = parseInput().split(" ");

        if(command[0].equals(UserCommand.ALL_CATEGORIES)){
            financeManager.showIncome();
            return;
        }

        financeManager.showIncome(Arrays.stream(command).toList());
    }

    private void handleGetExpensesReport(){
        System.out.printf("Выберите категории расходов для отчёта (введите названия через пробел).%n" +
                "Если хотите получить отчёт по всем категориям, введите команду %s%n", UserCommand.ALL_CATEGORIES);
        financeManager.showAllExpensesCategoriesNames();
        var command = parseInput().split(" ");

        if(command[0].equals(UserCommand.ALL_CATEGORIES)){
            financeManager.showExpenses();
            return;
        }

        financeManager.showExpenses(Arrays.stream(command).toList());
    }

    private void handleSetCategoryLimit(){
        System.out.println("Введите категорию расходов: ");
        var category = parseInput();
        System.out.println("Введите бюджет: ");
        var limit = 0d;
        try {
            limit = Double.parseDouble(parseInput());
        }
        catch(Exception  ex){
            System.out.println("ОШИБКА: Не удалось прочитать бюджет. Пожалуйста, попробуйте снова");
            handleSetCategoryLimit();
            return;
        }
        financeManager.setLimitForCategory(category, limit);
    }

    private void handleChangeWallet(){
        System.out.println("Введите название кошелька, в который желаете перейти: ");
        financeManager.showAllUserWalletsNames();
        var wallet = parseInput();
        if(!financeManager.setCurrentWallet(wallet)){
            System.out.println("Пожалуйста, повторите попыткку");
            handleChangeWallet();
        }
    }

    private void handleTransferBetweenWallets(){
        System.out.println("Выберите кошелёк, куда желаете перевести средства");
        financeManager.showAllUserWalletsNames();
        var wallet = parseInput();
        System.out.println("Введите сумму: ");;
        var sum = 0d;
        try{
            sum = Double.parseDouble(parseInput());
        }
        catch(Exception ex){
            System.out.println("ОШИБКА: Не удалось прочитать сумму. Пожалуйста, попробуйте снова");
            handleTransferBetweenWallets();
            return;
        }
        if(!financeManager.tryExecuteTransferBetweenWallets(wallet, sum)){
            System.out.println("Операция не удалась. Попробуйте снова");
            handleTransferBetweenWallets();
        }
    }

    private void handleTransferBetweenUsers(){
        System.out.println("Выберите пользователя, которому желаете перевести средства");
        var user = parseInput();
        System.out.println("Введите название кошелька пользователя, на который желаете перевести средства");
        var wallet = parseInput();
        System.out.println("Введите сумму: ");;
        var sum = 0d;
        try{
            sum = Double.parseDouble(parseInput());
        }
        catch(Exception ex){
            System.out.println("ОШИБКА: Не удалось прочитать сумму. Пожалуйста, попробуйте снова");
            handleTransferBetweenUsers();
            return;
        }
        if(!financeManager.tryExecuteTransferBetweenUsers(user, wallet, sum)){
            System.out.println("Операция не удалась. Попробуйте снова");
            handleTransferBetweenUsers();
        }
    }

    private void handleDeleteWallet(){
        System.out.println("Введите название кошелька, который желаете удалить");
        financeManager.showAllUserWalletsNames();

        var wallet = parseInput();
        if(!financeManager.tryDeleteWallet(wallet)){
            System.out.println("Операция не удалась. Попробуйте снова");
            handleDeleteWallet();
        }
    }

    private String parseInput(){
       var input =  scanner.nextLine();
       if(input.isEmpty()){
           System.out.println("ОШИБКА: вы ввели пустую команду");
           return parseInput();
       }
       checkOnCommands(input);
       return input;
    }

    private void checkOnCommands(String input){
        if(input.equals(UserCommand.QUIT)) {
            financeManager.saveChanges();
            System.exit(0);
        }
        if(input.equals(UserCommand.LOG_OUT)) {
            financeManager.saveChanges();
            run();
        }
        if(input.equals(UserCommand.PRINT_MENU)){
            printCommandMenu();
            handleUserCommand();
        }
    }

}

