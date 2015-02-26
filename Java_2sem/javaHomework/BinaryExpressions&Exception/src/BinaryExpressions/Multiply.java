package BinaryExpressions;

import MyExceptions.EvaluateException;
import MyExceptions.OverflowException;

public class Multiply extends BinaryOperation{
	public Multiply(Expression3 leftOperand, Expression3 rightOperand){
		super(leftOperand, rightOperand);
	}

	public int evaluate(int x, int y, int z) throws EvaluateException, ArithmeticException {
        int l = leftOperand.evaluate(x, y, z);
        int r = rightOperand.evaluate(x, y, z);
        if ((long) l * r > Integer.MAX_VALUE || (long) l * r < Integer.MIN_VALUE) {
            throw new OverflowException(String.valueOf((long) l * r));
        }
        return l * r;
	}
}
