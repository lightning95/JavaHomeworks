package Expressions;
public class Const extends Expression3{
	private final boolean value;

	public Const(boolean value){
		this.value = value;
	}

    public String toString() {
        return value ? "1" : "0";
    }

    public String name() {
        return null;
    }

    public Expression3 simplify() {
        return this;
    }

    public boolean getValue(){
        return value;
    }

    public Expression3 getOperand(){
        return null;
    }

    public Expression3 getLeftOperand(){
        return null;
    }

    public Expression3 getRightOperand(){
        return null;
    }
}
