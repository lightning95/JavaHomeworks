class ArrayQueueSingleton {
    // let "forAll i : l <= i < r"
    // if l < r -> forAll i : l <= i < r
    // if l > r -> forAll i : 0 <= i < r, l <= i < elements.length

    // inv :  0 <= l < elements.length, 0 <= r <= elements.length, l == r - Queue is Empty
    private static Object[] elements = new Object[10];
    private static int l = 0, r = 0;

    // elements[r] - Empty, 0 <= r <= elements.length
    public static void enqueue(Object obj) {
        ensureCapacity();
        elements[r++ % elements.length] = obj;
        r %= elements.length;
    }
    // 0 <= r <= elements.length, r = (r' + 1) % elements.length
    // forAll i' : l' <= i' < r'  elements[i'] ==
    // forAll i  : l <= i < r     elements[i]

    // 0 <= l < elements.length, 0 <= r <= elements.length
    private static void ensureCapacity() {
        if ((r + 1) % elements.length == l) {
            Object[] e = new Object[elements.length << 1];
            for (int i = l; l < r && i < r || l > r && i < elements.length; ++i) {
                e[i - l] = elements[i];
            }
            for (int i = 0; l > r && i < l; ++i) {
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
    public static Object dequeue() {
        assert !isEmpty();
        Object res = elements[l++];
        l %= elements.length;
        return res;
    }
    // result = elements[l'], l = (l' + 1) % elements.length, 0 <= l < elements.length

    // !isEmpty(), 0 <= l < elements.length
    public static Object peek() {
        assert !isEmpty();
        return elements[l];
    }
    // result = elements[l], l = l'

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