package Numbers;

/**
 * Created by Aydar on 20.04.14.
 */
public class IntegerNumber extends Number<IntegerNumber> {
    private int x;

    public IntegerNumber(int x) {
        this.x = x;
    }

    public IntegerNumber(String s) {
        this.x = Integer.parseInt(s);
    }

    public IntegerNumber multiply(IntegerNumber num) {
        return new IntegerNumber(x * num.x);
    }

    public IntegerNumber divide(IntegerNumber num) {
        return new IntegerNumber(x / num.x);
    }

    public IntegerNumber subtract(IntegerNumber num) {
        return new IntegerNumber(x - num.x);
    }

    public IntegerNumber add(IntegerNumber num) {
        return new IntegerNumber(x + num.x);
    }

    public IntegerNumber negate() {
        return new IntegerNumber(-x);
    }

    public IntegerNumber parse(String s) {
        return new IntegerNumber(s);
    }

    public String toString() {
        return String.valueOf(x);
    }

    public IntegerNumber abs() {
        return new IntegerNumber(Math.abs(x));
    }
}