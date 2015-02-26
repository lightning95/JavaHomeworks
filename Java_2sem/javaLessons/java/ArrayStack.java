public class ArrayStack extends AbstractStack {
	private Object[] elements = new Object[1];
	
	public void push(Object obj){
		ensureCapacity(size + 1);
		elements[size++] = obj;
	}

	private void ensureCapacity(int capacity){
		if (elements.length >= capacity){
			return;
		}
   		Object[] e = new Object[capacity << 1];
   		for (int i = 0; i < size; ++i){
   			e[i] = elements[i];
   		}		
   		elements = e;
	}

	protected Object popImpl(){
		return elements[--size];
	}
/*	public Object pop(){
		assert size > 0;
		Object result = elements[size - 1];
		elements[--size] = null;
		return result;
	}
	
	public Object peek(){
		assert size > 0;
		return elements[size - 1];
	}
    */
}