package Numbers;

import java.util.HashMap;

/**
 * Created by Aydar on 20.04.14.
 */
public abstract class Number<T> {
    public abstract T multiply(T num);

    public abstract T add(T num);

    public abstract T subtract(T num);

    public abstract T divide(T num);

    public abstract T negate();

    public abstract T parse(String s);

    public abstract T abs();

    public abstract String toString();

    public HashMap<String, Pair<Integer, FunctionExpression<T, T>>> functions() {
        HashMap<String, Pair<Integer, FunctionExpression<T, T>>> map = new HashMap<>();
        map.put("abs", new Pair<>(5, (T... arguments) -> abs()));
        map.put("+", new Pair<>(1, (T... arguments) -> add(arguments[0])));
        map.put("-", new Pair<>(1, (T... arguments) -> subtract(arguments[0])));
        map.put("*", new Pair<>(2, (T... arguments) -> multiply(arguments[0])));
        map.put("/", new Pair<>(2, (T... arguments) -> divide(arguments[0])));

        //map.put("-", (IntegerNumber... arguments) -> negate());
        map.put("cos", new Pair<Integer, FunctionExpression<T, T>>());
        return map;
    }
}

