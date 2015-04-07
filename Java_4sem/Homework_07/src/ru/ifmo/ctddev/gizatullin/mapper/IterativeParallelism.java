package ru.ifmo.ctddev.gizatullin.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Aydar Gizatullin a.k.a. lightning95 (aydar.gizatullin@gmail.com)
 *         Created on 3/19/15.
 */

public class IterativeParallelism implements ListIP {
    private ParallelMapper parallelMapper;

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    /**
     * Splits the list into threads.
     * Splits into threads the {@code list} with Runners, runs and waits them to die.
     *
     * @param list list of Runners to thread and execute
     * @see ru.ifmo.ctddev.gizatullin.mapper.IterativeParallelism.Runner
     * @see Thread
     * @see InterruptedException
     */
    private <T, R> void threader(List<Runner<T, R>> list) {
        List<Thread> threads = new ArrayList<>();
        list.stream().map(Thread::new).forEach(threads::add);
        threads.forEach(Thread::start);
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Class to apply {@code function} to {@code data}
     *
     * @see Runnable
     * @see java.util.function.Function
     */
    private class Runner<T, R> implements Runnable {
        private List<? extends T> data;
        private Function<List<? extends T>, R> function;
        private R result;

        Runner(List<? extends T> data, Function<List<? extends T>, R> function) {
            this.data = data;
            this.function = function;
        }

        @Override
        public void run() {
            result = function.apply(data);
        }

        public R getResult() {
            return result;
        }
    }

    private <T> List<List<? extends T>> splitter(List<? extends T> list, int p) {
        List<List<? extends T>> res = new ArrayList<>();
        int c = Math.max(list.size() / p, 1);
        for (int i = 0, n = list.size() / c; i < n; ++i) {
            res.add(list.subList(i * c, (i + 1) * c + (i + 1 == n ? list.size() % c : 0)));
        }
        return res;
    }

    /**
     * Applies function to the list.
     * Applies {@code function} to the {@code list} divided into {@code n} threads.
     *
     * @param n        number of threads to divide into
     * @param list     data to divide into threads and apply function to
     * @param function function to apply
     * @return {@code List} with results of the {@code function} for each {@code Thread}
     * @see #threader
     * @see ru.ifmo.ctddev.gizatullin.mapper.IterativeParallelism.Runner
     * @see java.util.function.Function
     */
    private <T, R> List<R> apply(int n, List<? extends T> list, Function<List<? extends T>, R> function) throws InterruptedException {
        if (parallelMapper != null) {
            return parallelMapper.map(function, splitter(list, n));
        }

        List<Runner<T, R>> threaded = new ArrayList<>();
        for (int i = 0, num = Math.max(list.size() / n, 1); i < Math.min(n, list.size()); ++i) {
            threaded.add(new Runner<>(list.subList(i * num, num * (i + 1) +
                    (i + 1 == n && list.size() >= n ? list.size() % n : 0)), function));
        }
        threader(threaded);
        return threaded.stream().map(Runner::getResult).collect(Collectors.toList());
    }

    /**
     * Finds maximum in the list.
     * Finds maximum in the {@code list} divided into {@code n} threads using {@code comparator}.
     *
     * @param n          number of threads to divide into
     * @param list       list to find maximum in
     * @param comparator comparator to compare elements in the list
     * @return maximum in the {@code list} using {@code comparator}
     * @throws InterruptedException
     * @see InterruptedException
     * @see #apply
     */
    @Override
    public <T> T maximum(int n, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> max = data -> data.stream().max(comparator).get();
        return max.apply(apply(n, list, max));
    }

    /**
     * Finds minimum in the list.
     * <p>
     * Finds minimum in the {@code list} divided into {@code n} threads using {@code comparator}.
     *
     * @param n          number of threads to divide into
     * @param list       list to find minimum in
     * @param comparator comparator to compare elements in the list
     * @return minimum in the {@code list} using {@code comparator}
     * @throws InterruptedException
     * @see InterruptedException
     * @see #apply
     */
    @Override
    public <T> T minimum(int n, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> min = data -> data.stream().min(comparator).get();
        return min.apply(apply(n, list, min));
    }

    /**
     * Checks the list for matching predicate.
     * <p>
     * Checks if every element in {@code list} matches {@code predicate}.
     *
     * @param n         number of threads to divide into
     * @param list      list to match predicate
     * @param predicate predicate to match
     * @return {@code true} if every element matches predicate, {@code false} otherwise
     * @throws InterruptedException
     * @see InterruptedException
     * @see #apply
     */
    @Override
    public <T> boolean all(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return apply(n, list, data -> data.stream().allMatch(predicate)).stream().allMatch(Predicate.isEqual(true));
    }

    /**
     * Checks the list for matching predicate.
     * <p>
     * Checks if any element in {@code list} matches {@code predicate}.
     *
     * @param n         number of threads to divide into
     * @param list      list to match predicate
     * @param predicate predicate to match
     * @return {@code true} if any element matches predicate, {@code false} otherwise
     * @throws InterruptedException
     * @see #apply
     * @see InterruptedException
     */
    @Override
    public <T> boolean any(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<Boolean> res = apply(n, list, data -> data.stream().anyMatch(predicate));
        return res.stream().anyMatch(Predicate.isEqual(true));
    }

    /**
     * Concatenates elements of the list.
     * <p>
     * Concatenates elements of the {@code list} as strings using {@code StringBuilder}.
     *
     * @param n    number of threads to divide into
     * @param list list to concatenate elements
     * @return {@code String} the concatenation of the elements of the list
     * @throws InterruptedException
     * @see #apply
     * @see StringBuilder
     * @see InterruptedException
     */
    @Override
    public String concat(int n, List<?> list) throws InterruptedException {
        StringBuilder ans = new StringBuilder();
        apply(n, list, data -> {
            StringBuilder result = new StringBuilder();
            data.stream().map(Object::toString).forEach(result::append);
            return result.toString();
        }).forEach(ans::append);
        return ans.toString();
    }

    /**
     * Filters the list with predicate.
     * Filters the {@code list} with the given {@code predicate}.
     *
     * @param n         number of threads to divide into
     * @param list      list to filter to
     * @param predicate predicate to filter the list
     * @return {@code List} of elements that match the {@code predicate}
     * @throws InterruptedException
     * @see #apply
     * @see InterruptedException
     */
    @Override
    public <T> List<T> filter(int n, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<T> ans = new ArrayList<>();
        apply(n, list, data -> {
            List<T> result = new ArrayList<>();
            data.stream().filter(predicate).forEach(result::add);
            return result;
        }).forEach(ans::addAll);
        return ans;
    }

    /**
     * Applies function to the list.
     * Applies {@code function} to {@code list} and returns {@code List} with results of the the {@code function}
     *
     * @param n        number of threads to divide into
     * @param list     list to map
     * @param function function to apply
     * @return {@code List} with {@code function}'s results
     * @throws InterruptedException
     * @see #apply
     * @see InterruptedException
     */
    @Override
    public <T, U> List<U> map(int n, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<U> res = new ArrayList<>();
        apply(n, list, data -> {
            List<U> result = new ArrayList<>();
            data.stream().map(function).forEach(result::add);
            return result;
        }).forEach(res::addAll);
        return res;
    }
}