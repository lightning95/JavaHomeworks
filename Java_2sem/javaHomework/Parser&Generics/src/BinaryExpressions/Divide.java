package BinaryExpressions;

import Numbers.Number;

public class Divide<T extends Number<T>> extends BinaryOperation<T> {
    public Divide(Expression3<T> leftOperand, Expression3<T> rightOperand) {
        super(leftOperand, rightOperand);
    }

    public T evaluate(T x, T y, T z) {
        T l = leftOperand.evaluate(x, y, z);
        T r = rightOperand.evaluate(x, y, z);
        return l.divide(r);
    }
}
