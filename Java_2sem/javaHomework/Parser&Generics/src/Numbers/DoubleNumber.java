package Numbers;

/**
 * Created by Aydar on 20.04.14.
 */
public class DoubleNumber implements Number<DoubleNumber> {
    private double x;

    public DoubleNumber(double x){
        this.x = x;
    }

    public DoubleNumber(String s){
        this.x = Double.parseDouble(s);
    }

    public DoubleNumber multiply(DoubleNumber num) {
        return new DoubleNumber(x * num.x);
    }

    public DoubleNumber divide(DoubleNumber num) {
        return new DoubleNumber(x  / num.x);
    }

    public DoubleNumber subtract(DoubleNumber num) {
        return new DoubleNumber(x - num.x);
    }

    public DoubleNumber add(DoubleNumber num) {
        return new DoubleNumber(x + num.x);
    }

    public DoubleNumber negate(){
        return new DoubleNumber(-x);
    }

    public DoubleNumber parse(String s){
        return new DoubleNumber(s);
    }

    public String toString(){
        return String.valueOf(x);
    }

    public DoubleNumber abs() {
        return new DoubleNumber(Math.abs(x));
    }
}