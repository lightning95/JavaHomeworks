package Expressions;

public abstract class Expression3{
    public abstract String toString();

    public abstract Expression3 simplify();

    public abstract String name();

    public abstract Expression3 getOperand();

    public abstract Expression3 getLeftOperand();

    public abstract Expression3 getRightOperand();

    public abstract boolean getValue();
}