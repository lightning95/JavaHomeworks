package BinaryExpressions;
public class Multiply extends BinaryOperation{
	public Multiply(Expression3 leftOperand, Expression3 rightOperand){
		super(leftOperand, rightOperand);
	}

	public int evaluate(int x, int y, int z){
		return leftOperand.evaluate(x, y, z) * rightOperand.evaluate(x, y, z);
	}
}
