package Numbers;

import BinaryExpressions.Expression3;

/**
 * Created by Aydar on 06.05.14.
 */
@FunctionalInterface
class FunctionExpression<T extends Number<T> > {
    interface Function<T extends Number<T> > {
        T apply(T arg);
    }
    public Function<T> functionToApply;
    public Expression3<T> argument;
}
