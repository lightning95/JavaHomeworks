class ArrayQueueADT {
    // let "forAll i : l <= i < r"
    // if l < r -> forAll i : l <= i < r
    // if l > r -> forAll i : 0 <= i < r, l <= i < elements.length

    // inv :  0 <= l < elements.length, 0 <= r <= elements.length, l == r - Queue is Empty
    private Object[] elements = new Object[10];
    private int l = 0, r = 0;

    // elements[r] - Empty, 0 <= r <= elements.length
    public static void enqueue(ArrayQueueADT queue, Object obj) {
        ensureCapacity(queue);
        queue.elements[queue.r++ % queue.elements.length] = obj;
        queue.r %= queue.elements.length;
    }
    // 0 <= r <= elements.length, r = (r' + 1) % elements.length
    // forAll i' : l' <= i' < r'  elements[i'] ==
    // forAll i  : l <= i < r     elements[i]

    // 0 <= l < elements.length, 0 <= r <= elements.length
    private static void ensureCapacity(ArrayQueueADT queue) {
        if ((queue.r + 1) % queue.elements.length == queue.l) {
            Object[] e = new Object[queue.elements.length << 1];
            for (int i = queue.l; queue.l < queue.r && i < queue.r || queue.l > queue.r && i < queue.elements.length; ++i) {
                e[i - queue.l] = queue.elements[i];
            }
            for (int i = 0; queue.l > queue.r && i < queue.l; ++i) {
                e[queue.elements.length - queue.l + i] = queue.elements[i];
            }
            queue.r = queue.elements.length - 1;
            queue.l = 0;
            queue.elements = e;
        }
    }
    // if r' - l' + 1 == elements'.length -> l = 0, r = elements'.length - 1, elements.length == 2 * elements'.length
    // else l' = l, r' = r, elements' = elements

    // !isEmpty(), 0 <= l < elements.length
    public static Object dequeue(ArrayQueueADT queue) {
        assert !isEmpty(queue);
        Object res = queue.elements[queue.l++];
        queue.l %= queue.elements.length;
        return res;
    }
    // result = elements[l'], l = (l' + 1) % elements.length, 0 <= l < elements.length

    // !isEmpty(), 0 <= l < elements.length
    public static Object peek(ArrayQueueADT queue) {
        assert !isEmpty(queue);
        return queue.elements[queue.l];
    }
    // result = elements[l], l = l'

    // 0 <= l < elements.length, 0 <= r <= elements.length
    public static boolean isEmpty(ArrayQueueADT queue) {
        return queue.l == queue.r;
    }
    // result = l == r

    // 0 <= l < elements.length, 0 <= r <= elements.length
    public static int size(ArrayQueueADT queue) {
        return queue.l <= queue.r ? queue.r - queue.l : queue.elements.length - queue.l + queue.r;
    }
    // result = elements number
}