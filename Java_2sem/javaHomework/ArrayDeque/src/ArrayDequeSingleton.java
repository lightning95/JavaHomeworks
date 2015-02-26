class ArrayDequeSingleton {
    // let
    // 1) "forAll i : l <= i < r" ==
    //  if l < r -> forAll i : l <= i < r
    //  if l > r -> forAll i : 0 <= i < r, l <= i < elements.length
    // 2) r + 1 == (r + 1) % elements.length
    // 3) l - 1 == (l - 1 + elements.length) % elements.length

    // inv : 0 <= l < elements.length, 0 <= r <= elements.length, l == r - Deque is Empty
    private static Object[] elements = new Object[10];
    private static int l = 0, r = 0;

    // elements[r] - Empty, 0 <= r <= elements.length
    public static void addLast(Object obj) {
        ensureCapacity();
        elements[r % elements.length] = obj;
        r = (r + 1) % elements.length;
    }
    // 0 <= r <= elements.length, r = (r' + 1) % elements.length, elements[r'] = obj
    // forAll i' : l' <= i' < r'  elements[i'] ==
    // forAll i  : l <= i < r     elements[i]

    // elements[l - 1] - Empty, 0 <= l < elements.length
    public static void addFirst(Object obj) {
        ensureCapacity();
        l = (l - 1 + elements.length) % elements.length;
        elements[l] = obj;
    }
    // 0 <= l < elements.length, l = (l' - 1 + elements.length) % elements.length, elements[l] = obj
    // forAll i' : l' <= i' < r'  elements[i'] ==
    // forAll i  : l <= i < r     elements[i]

    // 0 <= l < elements.length, 0 <= r <= elements.length
    private static void ensureCapacity() {
        if ((r + 1) % elements.length == l) {
            Object[] e = new Object[elements.length << 1];
            for (int i = l; l < r && i < r || l > r && i < elements.length; ++i) {
                e[i - l] = elements[i];
            }
            for (int i = 0; r < l && i < r; ++i) {
                e[elements.length - l + i] = elements[i];
            }
            r = elements.length - 1;
            l = 0;
            elements = e;
        }
    }
    // if r' - l' + 1 == elements'.length -> l = 0, r = elements'.length - 1, elements.length == 2 * elements'.length
    // else l' = l, r' = r, elements' = elements

    // !isEmpty(), 0 <= l < elements.length
    public static Object removeFirst() {
        assert !isEmpty();
        Object res = elements[l];
        l = (l + 1) % elements.length;
        return res;
    }
    // result = elements[l'], l = (l' + 1) % elements.length, 0 <= l < elements.length

    // !isEmpty(), 0 <= r <= elements.length
    public static Object removeLast() {
        assert !isEmpty();
        Object res = elements[(r - 1 + elements.length) % elements.length];
        r = (r - 1 + elements.length) % elements.length;
        return res;
    }
    // result = elements[r' - 1], r = (r' - 1) % elements.length, 0 <= r <= elements.length

    // !isEmpty(), 0 <= l < elements.length
    public static Object peekFirst() {
        assert !isEmpty();
        return elements[l];
    }
    // result = elements[l], l = l'

    // !isEmpty(), 0 <= r <= elements.length
    public static Object peekLast() {
        assert !isEmpty();
        return elements[(r - 1 + elements.length) % elements.length];
    }
    // result = elements[(r - 1 + elements.length) % elements.length], r = r'

    // 0 <= l < elements.length, 0 <= r <= elements.length
    public static boolean isEmpty() {
        return l == r;
    }
    // result = l == r

    // 0 <= l < elements.length, 0 <= r <= elements.length
    public static int size() {
        return l <= r ? r - l : elements.length - l + r;
    }
    // result = elements number


}