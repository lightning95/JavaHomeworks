public abstract class BinaryOperation implements Expression3{
	protected final Expression3 leftOperand;
	protected final Expression3 rightOperand;

	public BinaryOperation(Expression3 leftOperand, Expression3 rightOperand){
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public abstract double evaluate(double x, double y, double z);
}
