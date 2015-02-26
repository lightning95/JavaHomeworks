package BinaryExpressions;
public class Multiply extends BinaryOperation{
	public Multiply(Expression3 leftOperand, Expression3 rightOperand){
		super(leftOperand, rightOperand);
	}

	public int evaluate(int x, int y, int z){
		return leftOperand.evaluate(x, y, z) * rightOperand.evaluate(x, y, z);
	}

    public String toString() {
        String l = leftOperand.toString(), r = rightOperand.toString();
        if (leftOperand.getClass() == Add.class || leftOperand.getClass() == Subtract.class){
            l = "(" + l + ")";
        }
        if (rightOperand.getClass() == Add.class || rightOperand.getClass() == Subtract.class){
            r = "(" + r + ")";
        }
        return l + " * " + r;
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
            return new Const(leftOperand.value() * rightOperand.value());
        }
        if (leftOperand.getClass() == Const.class){
            if (leftOperand.value() == 0){
                return new Const(0);
            }
            if (leftOperand.value() == 1){
                return rightOperand;
            }
        }
        if (rightOperand.getClass() == Const.class){
            if (rightOperand.value() == 0){
                return new Const(0);
            }
            if (rightOperand.value() == 1){
                return leftOperand;
            }
        }

        return this;
    }
}
