package BinaryExpressions;

import Numbers.Number;

import java.util.HashMap;

public class Variable<T extends Number<T>> implements Expression3<T> {
    private final String name;
    private HashMap<String, Integer> map = new HashMap<>();

    public Variable(String name) {
        this.name = name;
    }

    public T evaluate(T... args){
        return args[map.get(name)];
    }
}
