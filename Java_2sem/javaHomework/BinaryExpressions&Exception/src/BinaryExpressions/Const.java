package BinaryExpressions;
public class Const implements Expression3{
	private final int value;

	public Const(int value){
		this.value = value;
	}
	
	public int evaluate(int x, int y, int z){
		return value;
	}
}
