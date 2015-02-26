package BinaryExpressions;

public interface Expression3{
    public int evaluate(int x, int y, int z);

    public String toString();

    public Expression3 simplify();

    public int value();

    public String name();
}