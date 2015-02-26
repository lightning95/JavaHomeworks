package BinaryExpressions;

public abstract class BinaryOperation implements Expression3 {
    protected Expression3 leftOperand;
    protected Expression3 rightOperand;

    public BinaryOperation(Expression3 leftOperand, Expression3 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    public abstract int evaluate(int x, int y, int z);

    public abstract String toString();

    public abstract Expression3 simplify();
}
