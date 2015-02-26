package BinaryExpressions;

import Numbers.Number;

public class Abs<T extends Number<T>> implements Expression3<T>{
	private final Expression3<T> operand;
	
	public Abs(Expression3<T> operand){
		this.operand = operand;
	}

    public T evaluate(T... args) {
        T op = operand.evaluate(args);
        return op.abs();
    }
}