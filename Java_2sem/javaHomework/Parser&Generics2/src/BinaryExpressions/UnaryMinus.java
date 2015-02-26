package BinaryExpressions;

import Numbers.Number;

public class UnaryMinus<T extends Number<T>> implements Expression3<T> {
    private final Expression3<T> operand;

    public UnaryMinus(Expression3<T> operand) {
        this.operand = operand;
    }

    public T evaluate(T... args){
        T op = operand.evaluate(args);
        return op.negate();
    }
}