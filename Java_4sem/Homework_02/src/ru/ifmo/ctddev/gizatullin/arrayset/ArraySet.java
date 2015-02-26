package ru.ifmo.ctddev.gizatullin.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private List<T> a;
    private Comparator<? super T> comparator;
    private boolean naturalOrder;
    private boolean reversed;

    public ArraySet() {
        a = new ArrayList<>(0);
        naturalOrder = true;
    }

    public ArraySet(Collection<T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        if (collection.isEmpty()) {
            a = new ArrayList<>(0);
        } else {
            a = new ArrayList<>(collection);
            Collections.sort(a, comparator);
            int last = 0;
            for (int i = 1; i < a.size(); ++i) {
                if (comparator.compare(a.get(last), a.get(i)) != 0){
                    a.set(++last, a.get(i));
                }
            }
            while (a.size() > last + 1) {
                a.remove(a.size() - 1);
            }
        }
    }

    public ArraySet(Collection<T> collection) {
        this(collection, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return ((Comparable<T>) o1).compareTo(o2);
            }
        });
        naturalOrder = true;
    }

    private ArraySet(List<T> list, Comparator<? super T> comparator, boolean naturalOrder, boolean reversed) {
        this.comparator = comparator;
        this.a = list;
        this.naturalOrder = naturalOrder;
        this.reversed = reversed;
    }

    @Override
    public T lower(T t) {
        int res = Collections.binarySearch(a, t, comparator);
        if (res < 0) {
            res = -(res + 1);
        }
        if (res > 0 && res <= a.size()) {
            return a.get(res - 1);
        }
        return null;
    }

    @Override
    public T floor(T t) {
        int res = Collections.binarySearch(a, t, comparator);
        if (res >= 0 && res < a.size()) {
            return a.get(res);
        }
        if (res < 0) {
            res = -(res + 1);
        }
        if (res > 0 && res <= a.size()) {
            return a.get(res - 1);
        }
        return null;
    }

    @Override
    public T ceiling(T t) {
        int res = Collections.binarySearch(a, t, comparator);
        if (res < 0) {
            res = -(res + 1);
        }
        if (res >= 0 && res < a.size()) {
            return a.get(res);
        }
        return null;
    }

    @Override
    public T higher(T t) {
        int res = Collections.binarySearch(a, t, comparator);
        if (res + 1 == a.size()) {
            return null;
        }
        if (res >= 0 && res + 1 < a.size()) {
            return a.get(res + 1);
        }
        if (res < 0) {
            res = -(res + 1);
        }
        if (res >= 0 && res < a.size()) {
            return a.get(res);
        }
        return null;
    }

    @Override
    public int size() {
        return a.size();
    }

    @Override
    public boolean isEmpty() {
        return a.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(a, (T) o, comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        if (reversed){
            return descendingIterator();
        }
        return new Iterator<T>() {
            private int curIndex = 0;

            @Override
            public boolean hasNext() {
                return curIndex < a.size();
            }

            @Override
            public T next() {
                return a.get(curIndex++);
            }
        };
    }

    @Override
    public Object[] toArray() {
        return a.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] array) {
        /* if (array == null){
            throw new NullPointerException();
        }*/
        return a.toArray(array);
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
        Collections.copy(tmp, a);
        Collections.reverse(tmp);

        return new ArraySet<>(tmp, comparator);
    }

    @Override
    public Iterator<T> descendingIterator() {
        if (reversed){
            return iterator();
        }

        return new Iterator<T>() {
            private int curIndex = a.size();

            @Override
            public boolean hasNext() {
                return curIndex > 0;
            }

            @Override
            public T next() {
                return a.get(--curIndex);
            }
        };
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        /*if (fromElement == null || toElement == null) {
            return new ArraySet<>(comparator, naturalOrder);
            //throw new NullPointerException();
        }

        if (comparator != null && comparator.compare(fromElement, toElement) > 0) {
            return new ArraySet<>(comparator, naturalOrder);
//            throw new IllegalArgumentException();
        }*/
        if (reversed){

        }

        int from = Collections.binarySearch(a, fromElement, comparator);
        int to = Collections.binarySearch(a, toElement, comparator);

        if (from >= 0 && from < a.size()) {
            if (!fromInclusive) {
                ++from;
            }
        }
        if (from < 0) {
            from = -(from + 1);
        }

        if (to >= 0 && to < a.size()) {
            if (!toInclusive) {
                --to;
            }
        }
        if (to < 0) {
            to = -(to + 1);
            --to;
        }

        return new ArraySet<>(a.subList(from, Math.max(to + 1, from)), comparator, naturalOrder, reversed);
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
        return naturalOrder ? null : comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (reversed){
            return subSet(toElement, false, fromElement, true);
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        if (reversed){
            tailSet(toElement, false);
        }
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        if (reversed){
            return headSet(fromElement, true);
        }
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        if (reversed){
            return a.get(a.size() - 1);
        }
        return a.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        if (reversed){
            return a.get(0);
        }
        return a.get(a.size() - 1);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }
}