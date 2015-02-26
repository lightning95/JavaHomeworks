package BinaryExpressions;

public class Variable implements Expression3{
    //	private final int value;
    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public int evaluate(int x, int y, int z) {
        return name.equals("x") ? x : name.equals("y") ? y : z;
    }

    public String toString() {
        return name;
    }

    public int value() {
        return 0;
    }

    public Expression3 simplify() {
        return this;
    }

    public String name() {
        return name;
    }
}
