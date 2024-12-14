package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveService {

    private final String filePath;

    public SaveService(String filePath){
        this.filePath = filePath;
    }

    public void saveUserData(List<User> users){

        var file = new File(filePath);
        if(file.exists()){
            file.delete();
        }

        try{
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(users);
        }
        catch (Exception ex){
            System.out.println("Ошибка: не удалось сохранить данные. " + ex.getMessage());
        }
    }

    public ArrayList<User> getSavedUserData() {

        var file = new File(filePath);
        if(!file.exists()){
            System.out.println("Отсутствует файл сохранения");
            return new ArrayList<>();
        }

        try{
            FileInputStream fileInputStream = new FileInputStream(filePath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
           return  (ArrayList) objectInputStream.readObject();
        }
        catch (Exception ex){
            System.out.println("ОШИБКА: Не удалось прочитать сохранение");
            return new ArrayList<>();
        }
    }

}
