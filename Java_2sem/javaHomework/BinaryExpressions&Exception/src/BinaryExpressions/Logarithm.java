package BinaryExpressions;

import MyExceptions.EvaluateException;
import MyExceptions.LogarithmException;

public class Logarithm implements Expression3 {
    private final Expression3 operand;
    public Logarithm(Expression3 operand) {
        this.operand = operand;
    }

    public int evaluate(int x, int y, int z) throws EvaluateException, ArithmeticException {
        int op = operand.evaluate(x, y, z);
        if (op < 1){
            throw new LogarithmException("Wrong logarithm operand: "+ String.valueOf(op));
        }
        return Integer.bitCount(Integer.highestOneBit(op) - 1);
        //return Integer.numberOfTrailingZeros(Integer.highestOneBit(op));
    }
}