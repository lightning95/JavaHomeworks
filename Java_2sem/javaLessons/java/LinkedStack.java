public class LinkedStack extends AbstractStack{
	private Node head;

	public void push(Object element){
		head = new Node(element, head);
	}

	protected Object popImpl(){
		Object res = head.value;
		head = head.next;
		return res;
	}

	private static class Node {
		private final Object value;
		private final Node next;
		

		public Node(Object v, Node n){
			value = v;
			next = n;
		}
	}

}
// final - determines once
// static - availabalnace of Outer this