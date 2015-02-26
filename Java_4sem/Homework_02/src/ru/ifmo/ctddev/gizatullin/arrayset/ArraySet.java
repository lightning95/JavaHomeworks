package ru.ifmo.ctddev.gizatullin.arrayset;

import java.util.*;

public class ArraySet<T> implements NavigableSet<T>, Iterable<T> {
    private List<T> a;
    private Comparator<? super T> comparator;
    private int left;
    private int right;

    private ArraySet(List<T> list, Comparator<? super T> comparator, int left, int right) {
        this.a = list;
        this.comparator = comparator;
        this.left = left;
        this.right = right;
    }

    private ArraySet(Comparator<? super T> comparator) {
        this.comparator = comparator;
        a = new ArrayList<>(0);
        left = 0;
        right = 0;
    }

    public ArraySet() {
        a = new ArrayList<>(0);
        left = 0;
        right = 0;
    }

    public ArraySet(Collection<T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        left = 0;
        if (collection.isEmpty()) {
            a = new ArrayList<>(0);
            right = 0;
        } else {
            a = new ArrayList<>();
            List<T> tmp = new ArrayList<>(collection);
            Collections.sort(tmp, comparator);
            T last = null;
            for (T t : tmp) {
                if (last == null || comparator != null && comparator.compare(t, last) != 0 ||
                        comparator == null && ((Comparable<T>) t).compareTo(last) != 0) {
                    last = t;
                    a.add(t);
                }
            }
            right = a.size();
        }
    }

    public ArraySet(Collection<T> collection) {
        this(collection, null);
    }

    @Override
    public T lower(T t) {
        List<T> cur = a.subList(left, right);
        int res = Collections.binarySearch(cur, t, comparator);
        if (res < 0) {
            res = -(res + 1);
        }
        if (res > 0 && res <= cur.size()) {
            return cur.get(res - 1);
        }
        return null;
    }

    @Override
    public T floor(T t) {
        List<T> cur = a.subList(left, right);
        int res = Collections.binarySearch(cur, t, comparator);
        if (res >= 0 && res < cur.size()) {
            return cur.get(res);
        }
        if (res < 0) {
            res = -(res + 1);
        }
        if (res > 0 && res <= cur.size()) {
            return cur.get(res - 1);
        }
        return null;
    }

    @Override
    public T ceiling(T t) {
        List<T> cur = a.subList(left, right);
        int res = Collections.binarySearch(cur, t, comparator);
        if (res < 0) {
            res = -(res + 1);
        }
        if (res >= 0 && res < cur.size()) {
            return cur.get(res);
        }
        return null;
    }

    @Override
    public T higher(T t) {
        List<T> cur = a.subList(left, right);
        int res = Collections.binarySearch(cur, t, comparator);
        if (res + 1 == cur.size()) {
            return null;
        }
        if (res >= 0 && res + 1 < cur.size()) {
            return cur.get(res + 1);
        }
        if (res < 0) {
            res = -(res + 1);
        }
        if (res >= 0 && res < cur.size()) {
            return cur.get(res);
        }
        return null;
    }

    @Override
    public int size() {
        return right - left;
    }

    @Override
    public boolean isEmpty() {
        return left == right;
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(a.subList(left, right), (T) o, comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentIndex = left;

            @Override
            public boolean hasNext() {
                return currentIndex < right;
            }

            @Override
            public T next() {
                return a.get(currentIndex++);
            }
        };
    }

    @Override
    public Object[] toArray() {
        return a.subList(left, right).toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] array) {
        /* if (array == null){
            throw new NullPointerException();
        }*/
        int size = size();
        T1[] res = array.length >= size ? array :
                (T1[]) java.lang.reflect.Array
                        .newInstance(array.getClass().getComponentType(), size);

        return a.subList(left, right).toArray(res);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object t : c) {
            if (!contains(t)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NavigableSet<T> descendingSet() {
        // TODO
        List<T> tmp = new ArrayList<>();
        Collections.copy(tmp, a.subList(left, right));
        Collections.reverse(tmp);

        return new ArraySet<>(tmp, comparator);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new Iterator<T>() {
            private int currentIndex = right;

            @Override
            public boolean hasNext() {
                return currentIndex > left;
            }

            @Override
            public T next() {
                return a.get(--currentIndex);
            }
        };
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        if (fromElement == null || toElement == null) {
            return new ArraySet<>(comparator);
            //throw new NullPointerException();
        }

        if (comparator != null && comparator.compare(fromElement, toElement) > 0 ||
                comparator == null && ((Comparable<T>) fromElement).compareTo(toElement) > 0) {
            return new ArraySet<>(comparator);
//            throw new IllegalArgumentException();
        }

        List<T> cur = a.subList(left, right);
        int from = Collections.binarySearch(cur, fromElement, comparator);
        int to = Collections.binarySearch(cur, toElement, comparator);

        if (from >= 0 && from < cur.size()) {
            if (!fromInclusive) {
                ++from;
            }
        }
        if (from < 0) {
            from = -(from + 1);
        }

        if (to >= 0 && to < cur.size()) {
            if (!toInclusive) {
                --to;
            }
        }
        if (to < 0) {
            to = -(to + 1);
            --to;
        }

        if (to - from + 1 > 0) {
            return new ArraySet<>(a, comparator, from, to + 1);
        }
        return new ArraySet<>(comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return subSet(!isEmpty() ? first() : null, true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return subSet(fromElement, inclusive, !isEmpty() ? last() : null, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return a.get(left);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return a.get(right - 1);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
}
