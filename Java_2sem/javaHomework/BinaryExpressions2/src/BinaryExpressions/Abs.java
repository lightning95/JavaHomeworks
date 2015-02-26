package BinaryExpressions;
public class Abs implements Expression3{
	private final Expression3 operand;
	
	public Abs(Expression3 operand){
		this.operand = operand;
	}

	public int evaluate(int x, int y, int z){
		return Math.abs(operand.evaluate(x, y, z));
	}
}