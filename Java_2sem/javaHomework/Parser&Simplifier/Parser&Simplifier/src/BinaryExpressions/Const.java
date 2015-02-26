package BinaryExpressions;
public class Const implements Expression3{
	private final int value;

	public Const(int value){
		this.value = value;
	}
	
	public int evaluate(int x, int y, int z){
		return value;
	}

    public String toString() {
        return "" + value;
    }

    public String name() {
        return null;
    }

    public Expression3 simplify() {
        return this;
    }

    public int value() {
        return value;
    }
}
