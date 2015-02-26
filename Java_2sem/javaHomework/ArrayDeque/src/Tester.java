class Tester{
	public static void main(String[] args){
	 	ArrayDeque deque = new ArrayDeque();
		for (String s : args){
			for (String t : s.split("\\s")){
				if (!t.isEmpty()){
					deque.addLast(Integer.parseInt(t));	
				}
			} 
		}
		int min = MinDeque.min(deque);
		System.out.println(min);	
	}
}