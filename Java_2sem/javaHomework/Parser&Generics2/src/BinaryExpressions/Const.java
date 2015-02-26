package BinaryExpressions;

import Numbers.Number;

public class Const<T extends Number<T>> implements Expression3<T> {
    private final T value;

    public Const(T value) {
        this.value = value;
    }

    public T evaluate(T... args) {
        return value;
    }
}
