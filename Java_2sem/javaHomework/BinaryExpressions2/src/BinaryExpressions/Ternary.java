package BinaryExpressions;
public class Ternary implements Expression3{
	private final Expression3 condition;
	private final Expression3 leftOperand;	
	private final Expression3 rightOperand;

	Ternary(Expression3 condition, Expression3 leftOperand, Expression3 rightOperand){
		this.condition = condition;
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;	
	}

	public int evaluate(int x, int y, int z){
	 	return Math.abs(condition.evaluate(x, y, z)) < 1e-9 ? leftOperand.evaluate(x, y, z) : rightOperand.evaluate(x, y, z); 
	}
}