package BinaryExpressions;
public class Not implements Expression3{
	private final Expression3 operand;
	
	public Not(Expression3 operand){
		this.operand = operand;
	}

	public int evaluate(int x, int y, int z){
		return ~operand.evaluate(x, y, z);
	}
}