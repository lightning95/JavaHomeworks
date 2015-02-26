package BinaryExpressions;

public class Add extends BinaryOperation {
    public Add(Expression3 leftOperand, Expression3 rightOperand) {
        super(leftOperand, rightOperand);
    }

    public int evaluate(int x, int y, int z) {
        return leftOperand.evaluate(x, y, z) + rightOperand.evaluate(x, y, z);
    }

    public String toString() {
        return leftOperand.toString() + " + " + rightOperand.toString();
    }

    public int value() {
        return 0;
    }

    public String name(){
        return null;
    }

    public Expression3 simplify() {
        leftOperand = leftOperand.simplify();
        rightOperand = rightOperand.simplify();
        if (leftOperand.getClass() == Const.class && rightOperand.getClass() == Const.class){
            return new Const(leftOperand.value() + rightOperand.value());
        }
        if (leftOperand.getClass() == Const.class && leftOperand.value() == 0){
            return rightOperand;
        }
        if (rightOperand.getClass() == Const.class && rightOperand.value() == 0){
            return leftOperand;
        }

        return this;
    }
}
