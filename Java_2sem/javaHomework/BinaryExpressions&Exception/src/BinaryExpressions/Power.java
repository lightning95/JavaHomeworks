package BinaryExpressions;

import MyExceptions.*;

public class Power extends BinaryOperation {
    public Power(Expression3 leftOperand, Expression3 rightOperand) {
        super(leftOperand, rightOperand);
    }

    public int evaluate(int x, int y, int z) throws ArithmeticException, EvaluateException {
        int l = leftOperand.evaluate(x, y, z);
        int r = rightOperand.evaluate(x, y, z);
        if (r < 1) {
            throw new PowerException("Power operand less than 0: " + String.valueOf(r));
        }
        long res = (long) Math.pow(l, r);
        if (res > Integer.MAX_VALUE || res < Integer.MIN_VALUE) {
            throw new OverflowException(String.valueOf(res));
        }
        return (int) res;
    }
}
