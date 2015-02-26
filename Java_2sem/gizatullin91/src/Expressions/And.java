package Expressions;
public class And extends BinaryOperation{
	public And(Expression3 leftOperand, Expression3 rightOperand){
		super(leftOperand, rightOperand);
	}

    public String toString() {
        String l = leftOperand.toString(), r = rightOperand.toString();
        if (leftOperand.getClass() == Or.class){
            l = "(" + l + ")";
        }
        if (rightOperand.getClass() == Or.class){
            r = "(" + r + ")";
        }
        return l + " & " + r;
    }

    public String name() {
        return null;
    }

    public Expression3 simplify() {
        leftOperand = leftOperand.simplify();
        rightOperand = rightOperand.simplify();
        return new And(leftOperand, rightOperand);
    }
}
