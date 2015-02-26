public class StackTest{
	public static void main(String[] args){
		ArrayStack stack = new ArrayStack();
		for (int i = 0; i < 5; ++i){
			stack.push(i);
		}
		while (!stack.isEmpty()){
			System.out.println(stack.pop());
		}		
	}
}
