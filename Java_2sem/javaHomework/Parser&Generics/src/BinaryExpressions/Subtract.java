package BinaryExpressions;

import Numbers.Number;

public class Subtract<T extends Number<T>> extends BinaryOperation<T>{
	public Subtract(Expression3<T> leftOperand, Expression3<T> rightOperand){
		super(leftOperand, rightOperand);
	}

	public T evaluate(T x, T y, T z) {
        T l = leftOperand.evaluate(x, y, z);
        T r = rightOperand.evaluate(x, y, z);
        return l.subtract(r);
	}
}
