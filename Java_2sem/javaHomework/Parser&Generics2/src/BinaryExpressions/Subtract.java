package BinaryExpressions;

import Numbers.Number;

public class Subtract<T extends Number<T>> extends BinaryOperation<T>{
	public Subtract(Expression3<T> leftOperand, Expression3<T> rightOperand){
		super(leftOperand, rightOperand);
	}

	public T evaluate(T... args) {
        T l = leftOperand.evaluate(args);
        T r = rightOperand.evaluate(args);
        return l.subtract(r);
	}
}
