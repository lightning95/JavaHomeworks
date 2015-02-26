package BinaryExpressions;

import Numbers.Number;

public class Multiply<T extends Number<T>> extends BinaryOperation<T> {
    public Multiply(Expression3<T> leftOperand, Expression3<T> rightOperand) {
        super(leftOperand, rightOperand);
    }

    public T evaluate(T x, T y, T z) {
        T l = leftOperand.evaluate(x, y, z);
        T r = rightOperand.evaluate(x, y, z);
        return l.multiply(r);
    }
}
