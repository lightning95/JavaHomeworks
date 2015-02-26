public class Const implements Expression3{
	private final double value;

	public Const(double value){
		this.value = value;
	}
	
	public double evaluate(double x, double y, double z){
		return value;
	}
}
