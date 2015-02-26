package BinaryExpressions;

import Numbers.Number;

public interface Expression3<T extends Number<T>>{
    public T evaluate(T x, T y, T z);
}