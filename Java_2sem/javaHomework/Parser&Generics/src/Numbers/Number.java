package Numbers;

/**
 * Created by Aydar on 20.04.14.
 */
public interface Number<T> {
    public T multiply(T num);
    public T add(T num);
    public T subtract(T num);
    public T divide(T num);
    public T negate();
    public T parse(String s);
	public T abs();
	public String toString();
}
