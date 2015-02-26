package BinaryExpressions;

import Numbers.Number;

public class Variable<T extends Number<T>> implements Expression3<T> {
    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public T evaluate(T x, T y, T z){
        return name.equals("x") ? x : name.equals("y") ? y : z;
    }
}
