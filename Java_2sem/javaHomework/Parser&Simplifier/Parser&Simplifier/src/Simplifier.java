import BinaryExpressions.*;

/**
 * Created by Aydar on 05.04.14.
 */
public class Simplifier {
    public static void main(String[] args){
        String s = args.toString();
//        s = "2 * (x + 1) - 0";
        System.out.println(new ExpressionParser().parse(s).simplify().toString());
    }
}
