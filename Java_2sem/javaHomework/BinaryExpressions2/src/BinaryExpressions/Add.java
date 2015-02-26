package BinaryExpressions;
public class Add extends BinaryOperation{
	public Add(Expression3 leftOperand, Expression3 rightOperand){
		super(leftOperand, rightOperand);
	}

	public int evaluate(int x, int y, int z){
		return leftOperand.evaluate(x, y, z) + rightOperand.evaluate(x, y, z);
	}
}
