class MinDeque{
	public static int min(ArrayDeque deque){
		int res = (Integer) deque.peekFirst();
        for (int i = 0; i < deque.size(); ++i){
        	Object x = deque.removeFirst();
			res = min(res, x);
			deque.addLast(x);
		}
		
        return res;
    }

    private static int min(int a, Object b) {
        return Math.min(a, (Integer) b);
    }
}