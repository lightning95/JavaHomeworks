public abstract class AbstractStack implements Stack{
	protected int size;

	public int size(){
		return size;
	}
	
	public boolean isEmpty(){
		return size == 0;
	}

	public Object peek(){
	 	Object res = pop();
		push(res);		
		return res;
	}

	public Object pop(){
	 	assert size > 0;
		--size;
		return popImpl();
	}

	protected abstract Object popImpl();
}

// protected - visible for ancestor
