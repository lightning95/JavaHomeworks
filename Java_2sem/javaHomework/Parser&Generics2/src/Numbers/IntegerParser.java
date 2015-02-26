package Numbers;

/**
 * Created by Aydar on 22.04.14.
 */
public class IntegerParser implements ConstantParser{
    public IntegerNumber parse(String s){
        return new IntegerNumber(s);
    }
}
