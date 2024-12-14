package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class User implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;
    private final List<UserWallet> wallets;

    public User(String login, String password){
        this.login = login;
        this.password = password;
        wallets = new ArrayList<>();
    }

    public String getLogin(){
        return login;
    }

    public String getPassword(){
        return password;
    }

    public void addWallet(String walletName){
        wallets.add(new UserWallet(walletName));
    };

    public Optional<UserWallet> getWallet(String walletName){
        return wallets.stream().filter(w -> w.getName().equals(walletName)).findAny();
    }

    public List<UserWallet> getAllWallets(){
        return wallets;
    }

    public void deleteWallet(UserWallet wallet){
        wallets.remove(wallet);
    }
}
