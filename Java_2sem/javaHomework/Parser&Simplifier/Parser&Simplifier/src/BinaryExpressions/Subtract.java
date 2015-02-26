package BinaryExpressions;

public class Subtract extends BinaryOperation {
    public Subtract(Expression3 leftOperand, Expression3 rightOperand) {
        super(leftOperand, rightOperand);
    }

    public int evaluate(int x, int y, int z) {
        return leftOperand.evaluate(x, y, z) - rightOperand.evaluate(x, y, z);
    }

    public String toString() {
        return leftOperand.toString() + " - " + rightOperand.toString();
    }

    public int value() {
        return 0;
    }

    public String name() {
        return null;
    }

    public Expression3 simplify() {
        leftOperand = leftOperand.simplify();
        rightOperand = rightOperand.simplify();
        if (leftOperand.getClass() == Const.class && rightOperand.getClass() == Const.class){
            return new Const(leftOperand.value() - rightOperand.value());
        }
        if (rightOperand.getClass() == Const.class && rightOperand.value() == 0){
            return leftOperand;
        }
        if (leftOperand.getClass() == Variable.class && rightOperand.getClass() == Variable.class
                && leftOperand.name() == rightOperand.name()){
            return new Const(0);
        }
        return this;
    }
}
