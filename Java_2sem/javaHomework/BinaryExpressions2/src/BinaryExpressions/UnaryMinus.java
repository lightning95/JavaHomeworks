package BinaryExpressions;
public class UnaryMinus implements Expression3{
	private final Expression3 operand;
	
	public UnaryMinus(Expression3 operand){
		this.operand = operand;
	}

	public int evaluate(int x, int y, int z){
		return -operand.evaluate(x, y, z);
	}
}