package BinaryExpressions;

import Numbers.Number;

public class Divide<T extends Number<T>> extends BinaryOperation<T> {
    public Divide(Expression3<T> leftOperand, Expression3<T> rightOperand) {
        super(leftOperand, rightOperand);
    }

    public T evaluate(T... args) {
        T l = leftOperand.evaluate(args);
        T r = rightOperand.evaluate(args);
        return l.divide(r);
    }
}
