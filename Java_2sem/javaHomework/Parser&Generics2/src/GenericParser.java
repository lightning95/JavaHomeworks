import BinaryExpressions.Expression3;
import ExpressionParser.ExpressionParser;
import Numbers.*;
import Numbers.Number;

public class GenericParser {
    <T extends Number<T>> void test(String s, ConstantParser<T> parser, Number<T> num) {
        Expression3<T> parse = new ExpressionParser<T>().parse(s, parser, num.functions());
        Number[] a = new Number[100];

        parse.evaluate((T[]) a);
    }

    public static void main(String[] args) {
        try {
            switch (args[0]) {
                case "-i":
                    new GenericParser().test(args[1], new IntegerParser(), new IntegerNumber(1));
                    break;
                case "-d":
                    new GenericParser().test(args[1], new DoubleParser(), new DoubleNumber(1));
                    break;
                case "-bi":
                    new GenericParser().test(args[1], new BigIntegerParser(), new BigIntegerNumber("1"));
                    break;
            }
        } catch (Throwable ignored) {

        }
    }
}
