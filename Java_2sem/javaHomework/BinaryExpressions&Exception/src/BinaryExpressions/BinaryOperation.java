package BinaryExpressions;

import MyExceptions.EvaluateException;
import MyExceptions.LogarithmException;
import MyExceptions.OverflowException;
import MyExceptions.PowerException;

public abstract class BinaryOperation implements Expression3{
	protected final Expression3 leftOperand;
	protected final Expression3 rightOperand;

	public BinaryOperation(Expression3 leftOperand, Expression3 rightOperand){
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public abstract int evaluate(int x, int y, int z) throws ArithmeticException, EvaluateException;
}
