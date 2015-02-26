public class Subtract extends BinaryOperation{
	public Subtract(Expression3 leftOperand, Expression3 rightOperand){
		super(leftOperand, rightOperand);
	}

	public double evaluate(double x, double y, double z){
		return leftOperand.evaluate(x, y, z) - rightOperand.evaluate(x, y, z);
	}
}
