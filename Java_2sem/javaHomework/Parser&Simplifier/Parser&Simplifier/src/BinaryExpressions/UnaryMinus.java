package BinaryExpressions;
public class UnaryMinus implements Expression3{
	Expression3 operand;
	
	public UnaryMinus(Expression3 operand){
		this.operand = operand;
	}

	public int evaluate(int x, int y, int z){
		return -operand.evaluate(x, y, z);
	}

    public String toString() {
        return "-" + operand.toString();
    }

    public int value() {
        return 0;
    }

    public String name() {
        return null;
    }

    public Expression3 simplify() {
        operand = simplify();
        return this;
    }
}