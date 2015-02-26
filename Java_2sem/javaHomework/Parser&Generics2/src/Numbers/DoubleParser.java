package Numbers;

/**
 * Created by Aydar on 22.04.14.
 */
public class DoubleParser implements ConstantParser{
    public DoubleNumber parse(String s){
        return new DoubleNumber(s);
    }
}
