package ExpressionParser;
/**
 * Created by Aydar on 31.03.14.
 */

import BinaryExpressions.*;
import MyExceptions.SyntaxException;

public class ExpressionParser {
    enum Lexeme {
        plus, minus, mul, num, open, close, div, var, end, pow, log
    }

    private static Lexeme curLex;
    private static String s, name;
    private static int pos;
    private static int num;

    private static Expression3 expr() throws SyntaxException {
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

    private ExpressionParser(){
    }

    private static Expression3 add() throws SyntaxException {
        Expression3 c = pow();
        while (curLex == Lexeme.mul || curLex == Lexeme.div) {
            Lexeme l = curLex;
            nextLexeme();
            if (l == Lexeme.mul) {
                c = new Multiply(c, pow());
            } else {
                c = new Divide(c, pow());
            }
        }
        return c;
    }

    private static Expression3 pow() throws SyntaxException {
        Expression3 c = mul();
        if (curLex == Lexeme.pow) {
            nextLexeme();
            c = new Power(c, pow());
        }
        return c;
    }

    private static Expression3 mul() throws SyntaxException {
        if (curLex == Lexeme.log) {
            nextLexeme();
            return new Logarithm(mul());
        } else if (curLex == Lexeme.minus) {
            nextLexeme();
            if (curLex == Lexeme.num){
                nextLexeme();
                return new Const(-num);
            }
            return new UnaryMinus(mul());
        } else if (curLex == Lexeme.num) {
            int cur = num;
            nextLexeme();
            return new Const(cur);
        } else if (curLex == Lexeme.var) {
            String cur = name;
            nextLexeme();
            return new Variable(cur);
        } else if (curLex == Lexeme.open) {
            nextLexeme();
            Expression3 cur = expr();
            if (curLex != Lexeme.close) {
                throw new SyntaxException("Not enough brackets");
            }
            nextLexeme();
            return cur;
        } else {
            throw new SyntaxException("Unexpected lexeme");
        }
    }

    private static void nextLexeme() throws SyntaxException {
        for (; pos < s.length() && Character.isWhitespace(s.charAt(pos)); ++pos) {
        }
        if (pos >= s.length()) {
            return;
        }
        char c = s.charAt(pos);
        Lexeme last = curLex;
        if (Character.isDigit(c)) {
            num = findNum();
            curLex = Lexeme.num;
            --pos;
        } else if (Character.isLetter(c)) {
            if (c == 'l' && s.charAt(pos + 1) == 'b') {
                curLex = Lexeme.log;
                ++pos;
            } else {
                curLex = Lexeme.var;
                name = findName();
                if (!name.equals("x") && !name.equals("y") && !name.equals("z")) {
                    throw new SyntaxException("Wrong constant name");
                }
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
        } else if (c == '^') {
            curLex = Lexeme.pow;
        } else {
            throw new SyntaxException("Unexpected symbol");
        }
        if (last == Lexeme.num || last == Lexeme.close || last == Lexeme.var) {
            if (curLex != Lexeme.mul && curLex != Lexeme.div && curLex != Lexeme.plus && curLex != Lexeme.minus
                    && curLex != Lexeme.end && curLex != Lexeme.close && curLex != Lexeme.pow) {
                throw new SyntaxException("Not enough operators");
            }
        }
        if (last == Lexeme.log) {
            if (curLex != Lexeme.open) {
                throw new SyntaxException("Logarithm without brackets");
            }
        }
        if (last == Lexeme.mul || last == Lexeme.div || last == Lexeme.plus || last == Lexeme.minus || last == Lexeme.pow) {
            if (curLex != Lexeme.num && curLex != Lexeme.open && curLex != Lexeme.var && curLex != Lexeme.log) {
                throw new SyntaxException("Not enough operands");
            }
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

    private static int findNum() throws SyntaxException {
        int l = pos;
        for (; pos < s.length() && Character.isDigit(s.charAt(pos)); ++pos) {
        }
        long res = Long.parseLong(s.substring(l, pos));
        if (curLex == Lexeme.minus && res == -(long)Integer.MIN_VALUE){

        } else if (res > Integer.MAX_VALUE || res < Integer.MIN_VALUE) {
            throw new SyntaxException("overflow");
        }
        return (int) res;
    }

    public static Expression3 parse(String t) throws SyntaxException {
        s = t + ".";
        pos = 0;
        nextLexeme();
        Expression3 res = expr();
        if (curLex != Lexeme.end) {
            throw new SyntaxException("Not enough brackets");
        }
        return res;
    }
}

