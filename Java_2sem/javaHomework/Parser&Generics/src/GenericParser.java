import BinaryExpressions.Expression3;
import ExpressionParser.ExpressionParser;
import Numbers.*;
import Numbers.Number;

public class GenericParser {
    <T extends Number<T>> void test(String s, ConstantParser<T> parser) {
        Expression3<T> parse = new ExpressionParser<T>().parse(s, parser);
        for (int i = -100; i <= 100; ++i) {
            for (int j = -100; j <= 100; ++j) {
                try {
                    System.out.print(parse.evaluate(parser.parse(Integer.toString(i)),
                            parser.parse(Integer.toString(j)), parser.parse("0")));
                } catch (ArithmeticException e) {
                    System.out.print("error");
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        try {
            switch (args[0]) {
                case "-i":
                    new GenericParser().test(args[1], new IntegerParser());
                    break;
                case "-d":
                    new GenericParser().test(args[1], new DoubleParser());
                    break;
                case "-bi":
                    new GenericParser().test(args[1], new BigIntegerParser());
                    break;
            }
        } catch (Throwable ignored) {

        }
    }
}
