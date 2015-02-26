package ExpressionParser;
/**
 * Created by Aydar on 31.03.14.
 */

import BinaryExpressions.*;
import Numbers.*;
import Numbers.Number;

import java.util.HashMap;

public class ExpressionParser<T extends Number<T>> {
    private static int pos;
    private ConstantParser<T> parser;
    private String[] lexemes;
    private HashMap<String, Pair<Integer, FunctionExpression<T, T>>> map;

    private Expression3<T> expr(int p) {
        Expression3<T> cur = get();

        while (lexemes[pos]){

        }
        return cur;
    }

    private Expression3<T> get() {
        Expression3<T> cur;
        if (lexemes[pos].equals("(")) {
            ++pos;
            cur = expr(0);
            ++pos;
            return cur;
        }
        if (lexemes[pos].length() > 1 && lexemes[pos].startsWith("-")) {
            return new Const<>(parser.parse(lexemes[pos]));
        }
        if (){

        }

        Pair<Integer, FunctionExpression<T, T>> pair = map.get(lexemes[pos]);

        return cur;
    }

    public Expression3<T> parse(String t, ConstantParser<T> parser, HashMap<String, Pair<Integer, FunctionExpression<T, T>>> map) {

        this.map = map;
        this.parser = parser;
        lexemes = (t + " .").split("\\S+");
        return expr(0);
    }
}

class Abs<T extends Number<T>> implements Expression3<T>{
    private final Expression3<T> operand;

    public Abs(Expression3<T> operand){
        this.operand = operand;
    }

    public T evaluate(T... args) {
        T op = operand.evaluate(args);
        return op.abs();
    }
}