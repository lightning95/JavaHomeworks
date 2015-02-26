import Expressions.*;

public class ExpressionParser {
    enum Lexeme {
        or, and, num, open, close, var, not;
    }

    private static Lexeme curLex;
    private static String s, name;
    private static int pos;
    private static boolean num;

    private static Expression3 expr() {
        Expression3 c = add();
        while (curLex == Lexeme.or) {
            nextLexeme();
            c = new Or(c, add());
        }
        return c;
    }

    private static Expression3 add() {
        Expression3 c = mul();
        while (curLex == Lexeme.and) {
            nextLexeme();
            c = new And(c, mul());
        }
        return c;
    }

    private static Expression3 mul() {
        if (curLex == Lexeme.not) {
            nextLexeme();
            return new Negate(mul());
        } else if (curLex == Lexeme.num) {
            boolean cur = num;
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
            curLex = Lexeme.var;
            name = findName();
            return;
        } else if (c == '(') {
            curLex = Lexeme.open;
        } else if (c == '~') {
            curLex = Lexeme.not;
        } else if (c == '|') {
            curLex = Lexeme.or;
        } else if (c == '&') {
            curLex = Lexeme.and;
        } else if (c == ')') {
            curLex = Lexeme.close;
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

    private static boolean findNum() {
        int l = pos;
        for (; pos < s.length() && Character.isDigit(s.charAt(pos)); ++pos) {
        }
        return s.substring(l, pos).equals("1");
    }

    public static Expression3 parse(String t) {
        s = t;
        pos = 0;
        nextLexeme();
        return expr();
    }
}

