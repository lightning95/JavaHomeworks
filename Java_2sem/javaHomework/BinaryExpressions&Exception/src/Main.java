/**
 * Created by Aydar on 14.04.14.
 */

import BinaryExpressions.Expression3;
import MyExceptions.*;
import ExpressionParser.ExpressionParser;

public class Main {
    public static void main(String[] args) {
        try {
            Expression3 parse = ExpressionParser.parse("x ^ 5 + y * lb(x ^ 2 + 3)");
            for (int i = 0; i < 100; ++i) {
                for (int j = 0; j < 100; ++j) {
                    try {
                        System.out.println(parse.evaluate(i, j, 0));
                    } catch (ArithmeticException e) {
                        System.out.println("division by zero");
                    } catch (OverflowException e) {
                        System.out.println("overflow");
                    } catch (EvaluateException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        } catch (SyntaxException e) {
            System.out.println(e.getMessage());
        }
    }
}
