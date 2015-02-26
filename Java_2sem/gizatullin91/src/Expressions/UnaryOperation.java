package Expressions;

public abstract class UnaryOperation extends Expression3 {
    Expression3 operand;

    public UnaryOperation(Expression3 operand){
        this.operand = operand;
    }

    public abstract String toString();

    public abstract Expression3 simplify();

    public Expression3 getOperand(){
        return operand;
    }

    public Expression3 getRightOperand(){
        return null;
    }

    public Expression3 getLeftOperand(){
        return null;
    }

    public boolean getValue(){
        return false;
    }
}