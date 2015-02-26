package ExpressionParser;
/**
 * Created by Aydar on 31.03.14.
 */

import BinaryExpressions.*;
import Numbers.ConstantParser;
import Numbers.Number;
import java.util.HashMap;

public class ExpressionParser<T extends Number<T>> {
    enum Lexeme {
        plus, minus, mul, num, open, close, div, var, end, abs
    }

    //HashMap<String, Lexeme> mapStrLex;
    private static Lexeme curLex;
    private static String s, name;
    private static int pos;
    private T num;
    private ConstantParser<T> parser;

    private Expression3<T> expr() {
        Expression3<T> c = add();
        while (curLex == Lexeme.minus || curLex == Lexeme.plus) {
            Lexeme l = curLex;
            nextLexeme();
            if (l == Lexeme.plus) {
                c = new Add<>(c, add());
            } else {
                c = new Subtract<>(c, add());
            }
        }
        return c;
    }

    private Expression3<T> add() {
        Expression3<T> c = mul();
        while (curLex == Lexeme.mul || curLex == Lexeme.div) {
            Lexeme l = curLex;
            nextLexeme();
            if (l == Lexeme.mul) {
                c = new Multiply<>(c, mul());
            } else {
                c = new Divide<>(c, mul());
            }
        }
        return c;
    }

    private Expression3<T> mul() {
        if (curLex == Lexeme.abs) {
            nextLexeme();
            return new Abs<>(mul());
        } else if (curLex == Lexeme.minus) {
            nextLexeme();
            if (curLex == Lexeme.num) {
                nextLexeme();
                return new Const<>(num.negate());
            }
            return new UnaryMinus<>(mul());
        } else if (curLex == Lexeme.num) {
            T cur = num;
            nextLexeme();
            return new Const<>(cur);
        } else if (curLex == Lexeme.var) {
            String cur = name;
            nextLexeme();
            return new Variable<>(cur);
        } else if (curLex == Lexeme.open) {
            nextLexeme();
            Expression3<T> cur = expr();
            nextLexeme();
            return cur;
        } else {
            return null;
        }
    }

    private void nextLexeme() {
        for (; pos < s.length() && Character.isWhitespace(s.charAt(pos)); ++pos) {
        }
        if (pos >= s.length()) {
            return;
        }
        char c = s.charAt(pos);
        if (Character.isDigit(c)) {
            num = findNum();
            curLex = Lexeme.num;
            --pos;
        } else if (Character.isLetter(c)) {
            if (pos + 2 < s.length() && s.startsWith("abs", pos)) {
                curLex = Lexeme.abs;
                pos += 2;
            } else {
                //const
                curLex = Lexeme.var;
                name = findName();
                --pos;
            }
        } else if (c == '(') {
            curLex = Lexeme.open;
        } else if (c == '-') {
            curLex = Lexeme.minus;
        } else if (c == '+') {
            curLex = Lexeme.plus;
        } else if (c == '*') {
            curLex = Lexeme.mul;
        } else if (c == ')') {
            curLex = Lexeme.close;
        } else if (c == '/') {
            curLex = Lexeme.div;
        } else if (c == '.') {
            curLex = Lexeme.end;
        }
        ++pos;
    }

    private static String findName() {
        for (; pos < s.length() && !Character.isLetter(s.charAt(pos)); ++pos) {
        }
        int l = pos;
        for (; pos < s.length() && Character.isLetter(s.charAt(pos)); ++pos) {
        }
        return s.substring(l, pos);
    }

    private T findNum() {
        int l = pos;
        for (; pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == 'E' || s.charAt(pos) == 'e'
                || s.charAt(pos) == '-' || s.charAt(pos) == '.'); ++pos) {
        }
        return parser.parse(s.substring(l, pos));
    }

    public Expression3<T> parse(String t, ConstantParser<T> parser) {
        this.parser = parser;
        s = t;
        pos = 0;
        nextLexeme();
        return expr();
    }
}

