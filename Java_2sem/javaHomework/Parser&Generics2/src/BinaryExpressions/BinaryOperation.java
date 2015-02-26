package BinaryExpressions;

import Numbers.Number;

public abstract class BinaryOperation<T extends Number<T> > implements Expression3<T>{
	protected final Expression3<T> leftOperand;
	protected final Expression3<T> rightOperand;

	public BinaryOperation(Expression3<T> leftOperand, Expression3<T> rightOperand){
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}
}
