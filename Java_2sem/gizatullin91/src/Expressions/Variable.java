package Expressions;

public class Variable extends Expression3 {
    //	private final int value;
    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public Expression3 simplify() {
        return this;
    }

    public String name() {
        return name;
    }

    public Expression3 getOperand() {
        return null;
    }

    public boolean getValue() {
        return false;
    }

    public Expression3 getLeftOperand() {
        return null;
    }

    public Expression3 getRightOperand() {
        return null;
    }
}
