package Expressions;

public abstract class BinaryOperation extends Expression3 {
    Expression3 leftOperand;
    Expression3 rightOperand;

    public BinaryOperation(Expression3 leftOperand, Expression3 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    public abstract String toString();

    public abstract Expression3 simplify();

    public Expression3 getLeftOperand() {
        return leftOperand;
    }

    public Expression3 getRightOperand() {
        return rightOperand;
    }

    public Expression3 getOperand() {
        return null;
    }

    public boolean getValue(){
        return false;
    }
}
