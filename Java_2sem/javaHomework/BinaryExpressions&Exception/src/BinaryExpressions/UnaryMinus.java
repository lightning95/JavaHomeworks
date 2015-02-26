package BinaryExpressions;

import MyExceptions.*;

public class UnaryMinus implements Expression3 {
    private final Expression3 operand;

    public UnaryMinus(Expression3 operand) {
        this.operand = operand;
    }

    public int evaluate(int x, int y, int z) throws EvaluateException, ArithmeticException{
        int op = operand.evaluate(x, y, z);
        if (op == Integer.MIN_VALUE) {
            throw new OverflowException(String.valueOf(op));
        }
        return -operand.evaluate(x, y, z);
    }
}