import java.util.LinkedList;
import java.util.Random;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 * @version $$Id$$
 */
public class ArrayQueueTest {
    public static void main(String[] args) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        checkAssert();
        System.out.println("Testing ArrayQueueSingleton");
        testSingleQueue(new ArrayQueueSingletonAdapter());
        System.out.println("OK");

        System.out.println("Testing ArrayQueueADT");
        testSingleQueue(new ArrayQueueADTAdapter());
        testMultiQueue(new ArrayQueueADTAdapter(), new ArrayQueueADTAdapter());
        System.out.println("OK");

        System.out.println("Testing ArrayQueue");
        testSingleQueue(new ArrayQueueAdapter());
        testMultiQueue(new ArrayQueueAdapter(), new ArrayQueueAdapter());
        System.out.println("OK");
    }

    private static void checkAssert() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (!assertsEnabled) {
            throw new AssertionError("You should enable assertions by running 'java -ea ArrayQueueTest'");
        }
    }

    private static void testSingleQueue(Queue queue) {
        testJustCreated(queue);
        testSingleton(queue);
        testOrder(queue);
        testPreconditions(queue);
        for (int i : new int[]{10, 100, 10_000, 100_000}) {
            testCapacity(queue, i);
        }
        for (int i : new int[]{10, 100, 10_000, 100_000}) {
            testRandom(queue, i);
        }
    }

    private static void testRandom(Queue queue, int operations) {
        final Random random = new Random(2457023587203587234L);
        final LinkedList list = new LinkedList();
        System.out.println("    Testing random operations (operations = " + operations + ")");
        for (int i = 0; i < operations; i++) {
            randomOp(queue, list, random);
        }
        while (!list.isEmpty()) {
            assertEquals(queue.dequeue(), list.removeFirst(), "Dequeue");
        }
    }

    private static void randomOp(Queue queue, LinkedList list, Random random) {
        assertEquals(queue.isEmpty(), list.isEmpty(), "isEmpty");
        assertEquals(queue.size(), list.size(), "size");
        if (!list.isEmpty()) {
            assertEquals(queue.peek(), list.peekFirst(), "peekFirst");
        }
        if (list.isEmpty() || random.nextBoolean()) {
            Object element = random.nextInt();
            queue.enqueue(element);
            list.addLast(element);
        } else {
            assertEquals(queue.dequeue(), list.removeFirst(), "Dequeue");
        }
    }

    private static void testCapacity(Queue queue, int size) {
        System.out.println("    Testing capacity (size = " + size + ")");
        for (int i = 0; i < size; i++) {
            assertEquals(queue.size(), i, "Queue size");
            queue.enqueue("hello");
        }
        for (int i = 0; i < size; i++) {
            assertEquals(queue.size(), size - i, "Queue size");
            assertEquals(queue.peek(), "hello", "Illegal queue content");
            assertEquals(queue.dequeue(), "hello", "Illegal queue content");
        }
    }

    private static void testPreconditions(Queue queue) {
        System.out.println("    Testing queue methods preconditions");
        testPeekPrecondition(queue);
        testDequeuePrecondition(queue);
    }

    private static void testPeekPrecondition(Queue queue) {
        boolean ok = false;
        try {
            queue.peek();
        } catch (AssertionError e) {
            ok = true;
        }
        assert ok : "Precondition of peekFirst() should be checked";
    }

    private static void testDequeuePrecondition(Queue queue) {
        boolean ok = false;
        try {
            queue.dequeue();
        } catch (AssertionError e) {
            ok = true;
        }
        assert ok : "Precondition of peekFirst() should be checked";
    }

    private static void testOrder(Queue queue) {
        System.out.println("    Testing queue element order");
        queue.enqueue("a");
        queue.enqueue("b");
        assertEquals(queue.dequeue(), "a", "Queue must return elements in the order of addition (FIFO)");
        assertEquals(queue.dequeue(), "b", "Queue must return elements in the order of addition (FIFO)");
        assertTrue(queue.isEmpty(), "Queue should be empty");
    }

    private static void testSingleton(Queue queue) {
        System.out.println("    Testing singleton queue (queue containing single element)");
        queue.enqueue("a");
        assertTrue(!queue.isEmpty(), "Singleton queue must be non-empty");
        assertEquals(queue.size(), 1, "Singleton queue must have size 1");
        assertEquals(queue.peek(), "a", "peekFirst() for singleton queue should return sole element");
        assertEquals(queue.dequeue(), "a", "removeFirst() for singleton queue should return sole element");
        assertTrue(queue.isEmpty(), "Singleton queue should be empty after removeFirst()");
        assertTrue(queue.size() == 0, "Singleton queue should have zero size after removeFirst");
    }

    private static void testJustCreated(Queue queue) {
        System.out.println("    Testing just created queue");
        assertTrue(queue.isEmpty(), "Just created queue should be empty");
        assertTrue(queue.size() == 0, "Just created queue should have zero size");
    }

    private static void testMultiQueue(Queue queue1, Queue queue2) {
        System.out.println("    Testing two queues at once");
        final Random random = new Random(2457023587203587234L);
        final LinkedList list1 = new LinkedList();
        final LinkedList list2 = new LinkedList();
        for (int i = 0; i < 100_000; i++) {
            if (random.nextBoolean()) {
                randomOp(queue1, list1, random);
            } else {
                randomOp(queue2, list2, random);
            }
        }
    }

    private static void assertTrue(boolean condition, String message) {
        assert condition : message;
    }

    private static void assertEquals(Object actual, Object expected, String message) {
        assertTrue(
                actual == null && expected == null || actual != null && actual.equals(expected),
                String.format("%s: expected %s, found %s", message, expected, actual)
        );
    }

    private static class ArrayQueueSingletonAdapter implements Queue {
        public void enqueue(Object element) { ArrayQueueSingleton.enqueue(element); }
        public Object dequeue() { return ArrayQueueSingleton.dequeue(); }
        public Object peek() { return ArrayQueueSingleton.peek(); }
        public boolean isEmpty() { return ArrayQueueSingleton.isEmpty(); }
        public int size() { return ArrayQueueSingleton.size(); }
    }

    private static class ArrayQueueADTAdapter implements Queue {
        private final ArrayQueueADT queue = new ArrayQueueADT();
        public void enqueue(Object element) { ArrayQueueADT.enqueue(queue, element); }
        public Object dequeue() { return ArrayQueueADT.dequeue(queue); }
        public Object peek() { return ArrayQueueADT.peek(queue); }
        public boolean isEmpty() { return ArrayQueueADT.isEmpty(queue); }
        public int size() { return ArrayQueueADT.size(queue); }
    }

    private static class ArrayQueueAdapter implements Queue {
        private final ArrayQueue queue = new ArrayQueue();
        public void enqueue(Object element) { queue.enqueue(element); }
        public Object dequeue() { return queue.dequeue(); }
        public Object peek() { return queue.peek(); }
        public boolean isEmpty() { return queue.isEmpty(); }
        public int size() { return queue.size(); }
    }

    private interface Queue {
        void enqueue(Object element);
        Object dequeue();
        Object peek();
        boolean isEmpty();
        int size();
    }
}