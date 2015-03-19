package ru.ifmo.ctddev.gizatullin.iterativeparallelism;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 3/19/15.
 */

public class IterativeParallelism implements ListIP {

    /**
     * Threads the {@code list} with Runners, runs and waits them to die
     *
     * @param list of Runners to thread and run
     * @param <T>
     * @param <R>
     * @see ru.ifmo.ctddev.gizatullin.iterativeparallelism.IterativeParallelism.Runner
     * @see java.lang.Thread
     * @see java.lang.InterruptedException
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
     * @param <T>
     * @param <R>
     * @see java.lang.Runnable
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

    /**
     * Applies {@code function} to the {@code list} divided into {@code n} threads
     *
     * @param n number of threads to divide into
     * @param list data to divide into threads and apply function to
     * @param function to apply
     * @param <T>
     * @param <R>
     * @return list with results of the {@code function} for each {@code Thread}
     *
     * @see #threader
     * @see ru.ifmo.ctddev.gizatullin.iterativeparallelism.IterativeParallelism.Runner
     * @see java.util.function.Function
     */
    private <T, R> List<R> apply(int n, List<? extends T> list, Function<List<? extends T>, R> function) {
        List<Runner<T, R>> threaded = new ArrayList<>();
        for (int i = 0, num = list.size() / n; i < n; ++i) {
            threaded.add(new Runner<>(list.subList(i * num, num * (i + 1) + (i + 1 == n ? list.size() % n : 0)), function));
        }
        threader(threaded);
        List<R> result = new ArrayList<>();
        threaded.forEach(x -> result.add(x.getResult()));
        return result;
    }

    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> max = data -> data.stream().max(comparator).get();
        return max.apply(apply(i, list, max));
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> min = data -> data.stream().min(comparator).get();
        return min.apply(apply(i, list, min));
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return apply(i, list, data -> data.stream().allMatch(predicate)).stream().reduce(true, (a, b) -> a & b);
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return apply(i, list, data -> data.stream().anyMatch(predicate)).stream().reduce(false, (a, b) -> a | b);
    }

    @Override
    public String concat(int i, List<?> list) throws InterruptedException {
        StringBuilder ans = new StringBuilder();
        apply(i, list, data -> {
            StringBuilder result = new StringBuilder();
            data.stream().map(Object::toString).forEach(result::append);
            return result.toString();
        }).forEach(ans::append);
        return ans.toString();
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        List<T> ans = new ArrayList<>();
        apply(i, list, data -> {
            List<T> result = new ArrayList<>();
            data.stream().filter(predicate).forEach(result::add);
            return result;
        }).forEach(ans::addAll);
        return ans;
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        List<U> res = new ArrayList<>();
        apply(i, list, data -> {
            List<U> result = new ArrayList<>();
            data.stream().map(function).forEach(result::add);
            return result;
        }).forEach(res::addAll);
        return res;
    }
}