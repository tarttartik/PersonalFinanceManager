package org.example;

import java.io.Serial;
import java.io.Serializable;

public abstract class AbstractCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;

    public AbstractCategory(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
