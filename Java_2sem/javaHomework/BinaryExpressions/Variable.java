public class Variable implements Expression3{
//	private final double value;
	private final String name;

	public Variable(String name) {
		this.name = name;
	}
	
	public double evaluate(double x, double y, double z) {
		return name.equals("x") ? x : name.equals("y") ? y : z;
	}
}
