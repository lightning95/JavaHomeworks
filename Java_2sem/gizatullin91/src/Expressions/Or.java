package Expressions;

public class Or extends BinaryOperation {
    public Or(Expression3 leftOperand, Expression3 rightOperand) {
        super(leftOperand, rightOperand);
    }

    public String toString() {
        return leftOperand.toString() + " | " + rightOperand.toString();
    }

    public String name(){
        return null;
    }

    public Expression3 simplify() {
        leftOperand = leftOperand.simplify();
        rightOperand = rightOperand.simplify();
        return new Or(leftOperand, rightOperand);
    }
}
