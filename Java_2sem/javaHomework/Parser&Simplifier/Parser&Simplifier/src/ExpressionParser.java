/**
 * Created by Aydar on 31.03.14.
 */

import BinaryExpressions.*;

public class ExpressionParser {
    enum Lexeme {
        plus, minus, mul, num, open, close, div, var, not, abs;
    }

    private static Lexeme curLex;
    private static String s, name;
    private static int pos, num;

    private static Expression3 expr() {
        Expression3 c = add();
        while (curLex == Lexeme.minus || curLex == Lexeme.plus) {
            Lexeme l = curLex;
            nextLexeme();
            if (l == Lexeme.plus) {
                c = new Add(c, add());
            } else {
                c = new Subtract(c, add());
            }
        }
        return c;
    }

    private static Expression3 add() {
        Expression3 c = mul();
        while (curLex == Lexeme.mul || curLex == Lexeme.div) {
            Lexeme l = curLex;
            nextLexeme();
            if (l == Lexeme.mul) {
                c = new Multiply(c, mul());
            } else {
                c = new Divide(c, mul());
            }
        }
        return c;
    }

    private static Expression3 mul() {
      /*  if (curLex == Lexeme.abs) {
            nextLexeme();
            return new Abs(mul());
        } else if (curLex == Lexeme.not) {
            nextLexeme();
            return new Not(mul());
        } else*/ if (curLex == Lexeme.minus) {
            nextLexeme();
            return new UnaryMinus(mul());
        } else if (curLex == Lexeme.num) {
            int cur = num;
            nextLexeme();
            return new Const(cur);
        } else if (curLex == Lexeme.var) {
            String cur = name;
            nextLexeme();
            return new Variable(cur);
        } else {
            nextLexeme();
            Expression3 cur = expr();
            nextLexeme();
            return cur;
        }
    }

    private static void nextLexeme() {
        for (; pos < s.length() && Character.isWhitespace(s.charAt(pos)); ++pos) {
        }
        if (pos >= s.length()) {
            return;
        }
        char c = s.charAt(pos);
        if (Character.isDigit(c)) {
            curLex = Lexeme.num;
            num = findNum();
            return;
        } else if (Character.isLetter(c)) {
            if (c == 'a') {
                curLex = Lexeme.abs;
                pos += 2;
            } else {
                curLex = Lexeme.var;
                name = findName();
                return;
            }
        } else if (c == '(') {
            curLex = Lexeme.open;
        } else if (c == '~') {
            curLex = Lexeme.not;
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

    private static int findNum() {
        int l = pos;
        for (; pos < s.length() && Character.isDigit(s.charAt(pos)); ++pos) {
        }
        return (int) Long.parseLong(s.substring(l, pos));
    }

    public static Expression3 parse(String t) {
        s = t;
        pos = 0;
        nextLexeme();
        return expr();
    }
}

