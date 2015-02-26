package BinaryExpressions;
public class Subtract extends BinaryOperation{
	public Subtract(Expression3 leftOperand, Expression3 rightOperand){
		super(leftOperand, rightOperand);
	}

	public int evaluate(int x, int y, int z){
		return leftOperand.evaluate(x, y, z) - rightOperand.evaluate(x, y, z);
	}
}
