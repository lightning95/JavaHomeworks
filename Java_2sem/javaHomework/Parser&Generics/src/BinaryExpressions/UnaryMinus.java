package BinaryExpressions;

import Numbers.Number;

public class UnaryMinus<T extends Number<T>> implements Expression3<T> {
    private final Expression3<T> operand;

    public UnaryMinus(Expression3<T> operand) {
        this.operand = operand;
    }

    public T evaluate(T x, T y, T z){
        T op = operand.evaluate(x, y, z);
        return op.negate();
    }
}