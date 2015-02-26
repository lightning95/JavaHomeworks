package Numbers;

/**
 * Created by Aydar on 22.04.14.
 */
public class BigIntegerParser implements ConstantParser{
    public BigIntegerNumber parse(String s){
        return new BigIntegerNumber(s);
    }
}
