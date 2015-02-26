package BinaryExpressions;

import MyExceptions.*;

public interface Expression3{
    public int evaluate(int x, int y, int z) throws ArithmeticException, EvaluateException;
}