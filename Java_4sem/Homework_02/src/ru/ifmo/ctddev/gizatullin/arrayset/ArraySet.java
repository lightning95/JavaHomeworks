package ru.ifmo.ctddev.gizatullin.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private List<T> a;
    private Comparator<? super T> comparator;
    private boolean naturalOrder;

    public ArraySet() {
        this(null, true);
    }

    public ArraySet(Collection<T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        if (collection.isEmpty()) {
            a = new ReversingList<>(new ArrayList<T>(0), false);
        } else {
            ArrayList<T> tmp = new ArrayList<>(collection);
            Collections.sort(tmp, comparator);
            int last = 0;
            for (int i = 1; i < tmp.size(); ++i) {
                if (comparator.compare(tmp.get(last), tmp.get(i)) != 0) {
                    tmp.set(++last, tmp.get(i));
                }
            }
            while (tmp.size() > last + 1) {
                tmp.remove(tmp.size() - 1);
            }
            a = new ReversingList<>(tmp, false);
        }
    }

    public ArraySet(Collection<T> collection) {
        this(collection, new Comparator<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public int compare(T o1, T o2) {
                return ((Comparable<T>) o1).compareTo(o2);
            }
        });
        naturalOrder = true;
    }

    // emptyList
    private ArraySet(Comparator<? super T> comparator, boolean naturalOrder) {
        this.a = new ReversingList<>(new ArrayList<T>(0), false);
        this.comparator = comparator;
        this.naturalOrder = naturalOrder;
    }

    // alreadySorted
    private ArraySet(List<T> list, Comparator<? super T> comparator, boolean naturalOrder) {
        this.a = list;
        this.comparator = comparator;
        this.naturalOrder = naturalOrder;
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
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(a, (T) o, comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
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
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversingList<>(a, true), comparator);
    }

    @Override
    public Iterator<T> descendingIterator() {
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
        if (fromElement == null || toElement == null) {
            throw new NullPointerException();
        }

        if (comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
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

        return new ArraySet<>(a.subList(from, Math.max(to + 1, from)), comparator, naturalOrder);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (isEmpty() || comparator.compare(first(), toElement) > 0) {
            return new ArraySet<>(comparator, naturalOrder);
        }

        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (isEmpty() || comparator.compare(fromElement, last()) > 0) {
            return new ArraySet<>(comparator, naturalOrder);
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return naturalOrder ? null : comparator;
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
        return a.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
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

    class ReversingList<T1> extends AbstractList<T1> implements RandomAccess {
        private List<T1> data;
        private boolean reversed;

        ReversingList(List<T1> list, boolean reversed) {
            data = list;
            this.reversed = reversed;
        }

        @Override
        public T1 get(int index) {
            return reversed ? data.get(data.size() - index - 1) : data.get(index);
        }

        @Override
        public int size() {
            return data.size();
        }
    }
}