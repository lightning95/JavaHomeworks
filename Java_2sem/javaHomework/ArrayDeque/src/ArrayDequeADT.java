class ArrayDequeADT {
    // let
    // 1) "forAll i : l <= i < r" ==
    //  if l < r -> forAll i : l <= i < r
    //  if l > r -> forAll i : 0 <= i < r, l <= i < elements.length
    // 2) r + 1 == (r + 1) % elements.length
    // 3) l - 1 == (l - 1 + elements.length) % elements.length

    // inv : 0 <= l < elements.length, 0 <= r <= elements.length, l == r - Deque is Empty
    private Object[] elements = new Object[10];
    private int l = 0, r = 0;

    // elements[r] - Empty, 0 <= r <= elements.length
    public static void addLast(ArrayDequeADT deque, Object obj) {
        ensureCapacity(deque);
        deque.elements[deque.r % deque.elements.length] = obj;
        deque.r = (deque.r + 1) % deque.elements.length;
    }
    // 0 <= r <= elements.length, r = (r' + 1) % elements.length, elements[r'] = obj
    // forAll i' : l' <= i' < r'  elements[i'] ==
    // forAll i  : l <= i < r     elements[i]

    // elements[l - 1] - Empty, 0 <= l < elements.length
    public static void addFirst(ArrayDequeADT deque, Object obj) {
        ensureCapacity(deque);
        deque.l = (deque.l - 1 + deque.elements.length) % deque.elements.length;
        deque.elements[deque.l] = obj;
    }
    // 0 <= l < elements.length, l = (l' - 1 + elements.length) % elements.length, elements[l] = obj
    // forAll i' : l' <= i' < r'  elements[i'] ==
    // forAll i  : l <= i < r     elements[i]

    // 0 <= l < elements.length, 0 <= r <= elements.length
    private static void ensureCapacity(ArrayDequeADT deque) {
        if ((deque.r + 1) % deque.elements.length == deque.l) {
            Object[] e = new Object[deque.elements.length << 1];
            for (int i = deque.l; deque.l < deque.r && i < deque.r || deque.l > deque.r && i < deque.elements.length; ++i) {
                e[i - deque.l] = deque.elements[i];
            }
            for (int i = 0; deque.l > deque.r && i < deque.r; ++i) {
                e[deque.elements.length - deque.l + i] = deque.elements[i];
            }
            deque.r = deque.elements.length - 1;
            deque.l = 0;
            deque.elements = e;
        }
    }
    // if r' - l' + 1 == elements'.length -> l = 0, r = elements'.length - 1, elements.length == 2 * elements'.length
    // else l' = l, r' = r, elements' = elements

    // !isEmpty(), 0 <= l < elements.length
    public static Object removeFirst(ArrayDequeADT deque) {
        assert !isEmpty(deque);
        Object res = deque.elements[deque.l++];
        deque.l %= deque.elements.length;
        return res;
    }
    // result = elements[l'], l = (l' + 1) % elements.length, 0 <= l < elements.length

    // !isEmpty(), 0 <= r <= elements.length
    public static Object removeLast(ArrayDequeADT deque) {
        assert !isEmpty(deque);
        Object res = deque.elements[(deque.r - 1 + deque.elements.length) % deque.elements.length];
        deque.r = (deque.r - 1 + deque.elements.length) % deque.elements.length;
        return res;
    }
    // result = elements[r' - 1], r = (r' - 1) % elements.length, 0 <= r <= elements.length

    // !isEmpty(), 0 <= l < elements.length
    public static Object peekFirst(ArrayDequeADT deque) {
        assert !isEmpty(deque);
        return deque.elements[deque.l];
    }
    // result = elements[l], l = l'

    // !isEmpty(), 0 <= r <= elements.length
    public static Object peekLast(ArrayDequeADT deque) {
        assert !isEmpty(deque);
        return deque.elements[(deque.r - 1 + deque.elements.length) % deque.elements.length];
    }
    // result = elements[(r - 1 + elements.length) % elements.length], r = r'

    // 0 <= l < elements.length, 0 <= r <= elements.length
    public static boolean isEmpty(ArrayDequeADT deque) {
        return deque.l == deque.r;
    }
    // result = l == r

    // 0 <= l < elements.length, 0 <= r <= elements.length
    public static int size(ArrayDequeADT deque) {
        return deque.l <= deque.r ? deque.r - deque.l : deque.elements.length - deque.l + deque.r;
    }
    // result = elements number
}