class UnaryMinus implements Expression3{
	private final Expression3 operand;
	
	UnaryMinus(Expression3 operand){
		this.operand = operand;
	}

	public double evaluate(double x, double y, double z){
		return -operand.evaluate(x, y, z);
	}
}