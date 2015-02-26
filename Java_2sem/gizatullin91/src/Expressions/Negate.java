package Expressions;
public class Negate extends UnaryOperation{
	public Negate(Expression3 operand){
        super(operand);
	}

    public String toString() {
        return "~" + operand.toString();
    }

    public String name() {
        return null;
    }

    public Expression3 simplify() {
        operand = operand.simplify();
        if (operand.getClass() == Const.class){
            return new Const(!operand.getValue()).simplify();
        }
        if (operand.getClass() == Negate.class){
            return operand.getOperand();
        }
        if (operand.getClass() == And.class){
            return new Or(new Negate(operand.getLeftOperand()), new Negate(operand.getRightOperand())).simplify();
        }
        if (operand.getClass() == Or.class){
            return new And(new Negate(operand.getLeftOperand()), new Negate(operand.getRightOperand())).simplify();
        }
        return this;
    }
}