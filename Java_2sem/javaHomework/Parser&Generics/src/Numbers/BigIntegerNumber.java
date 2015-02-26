package Numbers;

import java.math.BigInteger;

/**
 * Created by Aydar on 20.04.14.
 */
public class BigIntegerNumber implements Number<BigIntegerNumber> {
    private BigInteger x;

    public BigIntegerNumber(BigInteger x){
        this.x = x;
    }

    public BigIntegerNumber(String s){
        this.x = new BigInteger(s);
    }

    public BigIntegerNumber multiply(BigIntegerNumber num) {
        return new BigIntegerNumber(x.multiply( num.x));
    }

    public BigIntegerNumber divide(BigIntegerNumber num) {
        return new BigIntegerNumber(x.divide(num.x));
    }

    public BigIntegerNumber subtract(BigIntegerNumber num) {
        return new BigIntegerNumber(x.subtract(num.x));
    }

    public BigIntegerNumber add(BigIntegerNumber num) {
        return new BigIntegerNumber(x.add(num.x));
    }

    public BigIntegerNumber negate(){
        return new BigIntegerNumber(x.negate());
    }

    public BigIntegerNumber parse(String s){
        return new BigIntegerNumber(s);
    }

    public String toString(){
        return String.valueOf(x);
    }

    public BigIntegerNumber abs() {
        return new BigIntegerNumber(x.abs());
    }
}