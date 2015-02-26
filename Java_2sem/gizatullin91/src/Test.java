/**
 * Created by Aydar on 27.05.14.
 */
public class Test {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; ++i){
            sb.append(args[i]);
        }
        String s = sb.toString();
        System.out.println(new ExpressionParser().parse(s).simplify().toString());
    }
}
